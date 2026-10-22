#!/usr/bin/env bash
set -Eeuo pipefail
set +x

IMAGE_DIGEST="${1:?Usage: deploy-documind.sh sha256:DIGEST}"

# Deployment configuration
CONFIG_FILE="/etc/documind/deploy.conf"
ENV_FILE="/home/ubuntu/documind.env"
SECRET_ROOT="/etc/documind"

# AWS Parameter Store parameters
REDIS_PARAMETER="/documind/prod/redis-url"
OPENAI_PARAMETER="/documind/prod/openai-api-key"

# Container configuration
CONTAINER_NAME="documind"
PREVIOUS_NAME="documind-previous"
HEALTH_URL="http://127.0.0.1:8080/login"

AUTH_DIR=""
SECRETS_DIR=""

# Load deployment configuration
if [[ ! -f "$CONFIG_FILE" ]]; then
  echo "Deployment config not found: $CONFIG_FILE" >&2
  exit 1
fi

# Only use configuration stored on the EC2 instance.
source "$CONFIG_FILE"

# Validate deployment configuration
if [[ ! "${AWS_ACCOUNT_ID:-}" =~ ^[0-9]{12}$ ]]; then
  echo "Invalid AWS account ID in deployment config." >&2
  exit 1
fi

if [[ ! "${AWS_REGION:-}" =~ ^[a-z0-9-]+$ ]]; then
  echo "Invalid AWS region in deployment config." >&2
  exit 1
fi

if [[ ! "${ECR_REPOSITORY:-}" =~ ^[a-zA-Z0-9._/-]+$ ]]; then
  echo "Invalid ECR repository in deployment config." >&2
  exit 1
fi

if [[ -z "${EXPECTED_GITHUB_REPOSITORY:-}" ||
      -z "${EXPECTED_WORKFLOW:-}" ||
      -z "${EXPECTED_BRANCH:-}" ]]; then
  echo "Missing Cosign verification settings." >&2
  exit 1
fi

if [[ ! "$IMAGE_DIGEST" =~ ^sha256:[a-f0-9]{64}$ ]]; then
  echo "Invalid image digest." >&2
  exit 1
fi

if [[ "$EUID" -ne 0 ]]; then
  echo "Deployment script must run as root." >&2
  exit 1
fi

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Environment file not found: $ENV_FILE" >&2
  exit 1
fi

ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
EXPECTED_REPOSITORY="${ECR_REGISTRY}/${ECR_REPOSITORY}"
IMAGE_URI="${EXPECTED_REPOSITORY}@${IMAGE_DIGEST}"

EXPECTED_IDENTITY="https://github.com/${EXPECTED_GITHUB_REPOSITORY}/${EXPECTED_WORKFLOW}@refs/heads/${EXPECTED_BRANCH}"
EXPECTED_ISSUER="https://token.actions.githubusercontent.com"

# Return the secret directory label attached to a container, if present.
get_container_secret_dir() {
  local container_name="$1"

  if ! docker inspect "$container_name" >/dev/null 2>&1; then
    return 0
  fi

  docker inspect \
    --format '{{ with index .Config.Labels "my.documind.secrets-dir" }}{{ . }}{{ end }}' \
    "$container_name" 2>/dev/null || true
}

# Remove a secret directory only when no container references it.
cleanup_secret_dir_if_unused() {
  local dir="${1:-}"
  local references

  [[ -n "$dir" ]] || return 0

  if [[ "$dir" != "$SECRET_ROOT"/secrets.* ]]; then
    echo "Refusing to remove unexpected secret directory path." >&2
    return 1
  fi

  [[ -d "$dir" ]] || return 0

  if ! references="$(
    docker ps -aq --filter "label=my.documind.secrets-dir=$dir"
  )"; then
    echo "Could not verify secret directory references; keeping files." >&2
    return 1
  fi

  if [[ -n "$references" ]]; then
    echo "Secret directory is still referenced; keeping it."
    return 0
  fi

  rm -rf -- "$dir"
}

# Clean up temporary credentials and any unused new secret directory.
cleanup() {
  local exit_code=$?
  trap - EXIT

  if [[ -n "$AUTH_DIR" && -d "$AUTH_DIR" ]]; then
    rm -rf -- "$AUTH_DIR" || {
      echo "WARNING: Failed to remove temporary Docker credentials." >&2
    }
  fi

  if [[ -n "$SECRETS_DIR" ]]; then
    cleanup_secret_dir_if_unused "$SECRETS_DIR" || {
      echo "WARNING: Secret directory cleanup requires attention." >&2
    }
  fi

  exit "$exit_code"
}
trap cleanup EXIT

# Wait up to approximately two minutes for the application to respond.
wait_for_health() {
  local attempt

  for attempt in $(seq 1 24); do
    if curl --connect-timeout 2 --max-time 5 \
      -fsS "$HEALTH_URL" >/dev/null; then
      echo "Application health check passed."
      return 0
    fi

    sleep 5
  done

  return 1
}

# Restore the previous container and verify that it responds.
rollback_previous_container() {
  echo "Attempting to restore previous container."

  # Remove the failed new container, if it exists.
  if docker inspect "$CONTAINER_NAME" >/dev/null 2>&1; then
    if ! docker rm -f "$CONTAINER_NAME" >/dev/null; then
      echo "Failed to remove failed container." >&2
      return 1
    fi
  fi

  if ! docker inspect "$PREVIOUS_NAME" >/dev/null 2>&1; then
    echo "No previous container available for rollback." >&2
    return 1
  fi

  if ! docker rename "$PREVIOUS_NAME" "$CONTAINER_NAME"; then
    echo "Failed to rename previous container." >&2
    return 1
  fi

  if ! docker start "$CONTAINER_NAME"; then
    echo "Failed to start previous container." >&2
    return 1
  fi

  if ! wait_for_health; then
    echo "Previous container started but health check failed." >&2
    docker logs --tail 100 "$CONTAINER_NAME" >&2 || true
    return 1
  fi

  echo "Previous container restored and health check passed."
}

echo "Deploying image digest: $IMAGE_DIGEST"

# Use a temporary Docker configuration for ECR authentication.
AUTH_DIR="$(mktemp -d)"
chmod 700 "$AUTH_DIR"
export DOCKER_CONFIG="$AUTH_DIR"

# Authenticate to ECR.
aws ecr get-login-password --region "$AWS_REGION" |
  docker login \
    --username AWS \
    --password-stdin "$ECR_REGISTRY"

# Verify the image signature before pulling the image.
cosign verify \
  --certificate-identity="$EXPECTED_IDENTITY" \
  --certificate-oidc-issuer="$EXPECTED_ISSUER" \
  "$IMAGE_URI"

docker pull "$IMAGE_URI"

# Retrieve Parameter Store values before changing the current container.
umask 077
install -d -o root -g root -m 700 "$SECRET_ROOT"

if ! REDIS_URL="$(
  aws ssm get-parameter \
    --name "$REDIS_PARAMETER" \
    --with-decryption \
    --query 'Parameter.Value' \
    --output text \
    --region "$AWS_REGION"
)"; then
  echo "Failed to retrieve Redis URL from Parameter Store." >&2
  exit 1
fi

if ! OPENAI_API_KEY="$(
  aws ssm get-parameter \
    --name "$OPENAI_PARAMETER" \
    --with-decryption \
    --query 'Parameter.Value' \
    --output text \
    --region "$AWS_REGION"
)"; then
  unset REDIS_URL
  echo "Failed to retrieve OpenAI API key from Parameter Store." >&2
  exit 1
fi

# Validate the retrieved values without logging their contents.
if [[ -z "$REDIS_URL" ||
      "$REDIS_URL" == "None" ||
      ( "$REDIS_URL" != redis://* && "$REDIS_URL" != rediss://* ) ||
      "$REDIS_URL" == *$'\n'* ||
      "$REDIS_URL" == *$'\r'* ]]; then
  unset REDIS_URL OPENAI_API_KEY
  echo "Invalid Redis URL retrieved from Parameter Store." >&2
  exit 1
fi

if [[ -z "$OPENAI_API_KEY" ||
      "$OPENAI_API_KEY" == "None" ||
      "$OPENAI_API_KEY" == *$'\n'* ||
      "$OPENAI_API_KEY" == *$'\r'* ]]; then
  unset REDIS_URL OPENAI_API_KEY
  echo "Invalid OpenAI API key retrieved from Parameter Store." >&2
  exit 1
fi

# Store secrets in a dedicated directory mounted read-only by the container.
SECRETS_DIR="$(mktemp -d "$SECRET_ROOT/secrets.XXXXXX")"
chown root:root "$SECRETS_DIR"
chmod 700 "$SECRETS_DIR"

printf '%s' "$REDIS_URL" > "$SECRETS_DIR/spring.data.redis.url"
printf '%s' "$OPENAI_API_KEY" > "$SECRETS_DIR/openai.api-key"

chown root:root \
  "$SECRETS_DIR/spring.data.redis.url" \
  "$SECRETS_DIR/openai.api-key"

chmod 600 \
  "$SECRETS_DIR/spring.data.redis.url" \
  "$SECRETS_DIR/openai.api-key"

unset REDIS_URL OPENAI_API_KEY

# Ensure both secret files were created and are non-empty.
if [[ ! -s "$SECRETS_DIR/spring.data.redis.url" ||
      ! -s "$SECRETS_DIR/openai.api-key" ]]; then
  echo "Secret file validation failed." >&2
  exit 1
fi

# Record the secret directory used by the container that may be rolled back to.
ROLLBACK_SECRET_DIR=""

if docker inspect "$CONTAINER_NAME" >/dev/null 2>&1; then
  ROLLBACK_SECRET_DIR="$(get_container_secret_dir "$CONTAINER_NAME")"
elif docker inspect "$PREVIOUS_NAME" >/dev/null 2>&1; then
  ROLLBACK_SECRET_DIR="$(get_container_secret_dir "$PREVIOUS_NAME")"
fi

# Preserve the previous container before starting the new version.
if ! docker inspect "$CONTAINER_NAME" >/dev/null 2>&1; then
  if docker inspect "$PREVIOUS_NAME" >/dev/null 2>&1; then
    echo "Current container is missing. Previous container retained for rollback."
  fi
fi

if docker inspect "$CONTAINER_NAME" >/dev/null 2>&1; then
  if ! docker stop "$CONTAINER_NAME"; then
    echo "Failed to stop current container." >&2
    exit 1
  fi

  # Remove the stale backup before renaming the current container.
  if docker inspect "$PREVIOUS_NAME" >/dev/null 2>&1; then
    STALE_SECRET_DIR="$(get_container_secret_dir "$PREVIOUS_NAME")"

    if ! docker rm -f "$PREVIOUS_NAME" >/dev/null; then
      echo "Failed to remove stale previous container." >&2

      if ! docker start "$CONTAINER_NAME"; then
        echo "Failed to restart current container. Manual recovery required." >&2
      fi

      exit 1
    fi

    cleanup_secret_dir_if_unused "$STALE_SECRET_DIR" || {
      echo "WARNING: Stale secret directory cleanup failed." >&2
    }
  fi

  if ! docker rename "$CONTAINER_NAME" "$PREVIOUS_NAME"; then
    echo "Failed to preserve current container." >&2

    if ! docker start "$CONTAINER_NAME"; then
      echo "Failed to restart current container. Manual recovery required." >&2
    fi

    exit 1
  fi
fi

# Start the new container with the environment file and read-only secret mount.
if ! docker run -d \
  --name "$CONTAINER_NAME" \
  --restart unless-stopped \
  --env-file "$ENV_FILE" \
  --mount "type=bind,source=${SECRETS_DIR},target=/run/secrets,readonly" \
  --label "my.documind.secrets-dir=${SECRETS_DIR}" \
  -p 127.0.0.1:8080:8080 \
  "$IMAGE_URI"; then
  echo "Failed to start new container." >&2

  if ! rollback_previous_container; then
    echo "Automatic rollback failed. Manual recovery required." >&2
  fi

  exit 1
fi

# Verify the new application before removing the previous container.
if wait_for_health; then
  if docker inspect "$PREVIOUS_NAME" >/dev/null 2>&1; then
    if docker rm "$PREVIOUS_NAME" >/dev/null; then
      cleanup_secret_dir_if_unused "$ROLLBACK_SECRET_DIR" || {
        echo "WARNING: Previous secret directory cleanup failed." >&2
      }
    else
      echo "WARNING: Deployment succeeded, but previous container cleanup failed." >&2
    fi
  fi

  exit 0
fi

# If the new version fails its health check, log details and roll back.
echo "Health check failed. Rolling back." >&2
docker logs --tail 100 "$CONTAINER_NAME" >&2 || true

if ! rollback_previous_container; then
  echo "Automatic rollback failed. Manual recovery required." >&2
fi

exit 1
