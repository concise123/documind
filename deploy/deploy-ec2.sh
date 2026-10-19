
#!/usr/bin/env bash
set -Eeuo pipefail

IMAGE_DIGEST="${1:?Usage: deploy-documind.sh sha256:DIGEST}"

CONFIG_FILE="/etc/documind/deploy.conf"

if [[ ! -f "$CONFIG_FILE" ]]; then
  echo "Deployment config not found: $CONFIG_FILE" >&2
  exit 1
fi

# EC2에 저장된 설정만 사용
source "$CONFIG_FILE"

if [[ ! "$AWS_ACCOUNT_ID" =~ ^[0-9]{12}$ ]]; then
  echo "Invalid AWS account ID in deployment config." >&2
  exit 1
fi

if [[ ! "$AWS_REGION" =~ ^[a-z0-9-]+$ ]]; then
  echo "Invalid AWS region in deployment config." >&2
  exit 1
fi

if [[ ! "$ECR_REPOSITORY" =~ ^[a-zA-Z0-9._/-]+$ ]]; then
  echo "Invalid ECR repository in deployment config." >&2
  exit 1
fi

if [[ ! "$IMAGE_DIGEST" =~ ^sha256:[a-f0-9]{64}$ ]]; then
  echo "Invalid image digest." >&2
  exit 1
fi

ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
EXPECTED_REPOSITORY="${ECR_REGISTRY}/${ECR_REPOSITORY}"
IMAGE_URI="${EXPECTED_REPOSITORY}@${IMAGE_DIGEST}"

EXPECTED_IDENTITY="https://github.com/${EXPECTED_GITHUB_REPOSITORY}/${EXPECTED_WORKFLOW}@refs/heads/${EXPECTED_BRANCH}"
EXPECTED_ISSUER="https://token.actions.githubusercontent.com"

CONTAINER_NAME="documind"
PREVIOUS_NAME="documind-previous"
ENV_FILE="/home/ubuntu/documind.env"
HEALTH_URL="http://127.0.0.1:8080/login"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Environment file not found: $ENV_FILE" >&2
  exit 1
fi

echo "Deploying image digest: $IMAGE_DIGEST"

# ECR 로그인
aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "$ECR_REGISTRY"

# Cosign 서명 인증
cosign verify \
  --certificate-identity="${EXPECTED_IDENTITY}" \
  --certificate-oidc-issuer="${EXPECTED_ISSUER}" \
  "${IMAGE_URI}" \
  >/dev/null

echo "Image signature verified."

# 새 이미지 다운로드
docker pull "$IMAGE_URI"

# 이전 배포가 중단된 상태라면 기존 컨테이너 복구
if ! docker inspect "$CONTAINER_NAME" >/dev/null 2>&1 \
   && docker inspect "$PREVIOUS_NAME" >/dev/null 2>&1; then
  docker rename "$PREVIOUS_NAME" "$CONTAINER_NAME"
  docker start "$CONTAINER_NAME"
fi

# 기존 컨테이너 보존 후 중지
if docker inspect "$CONTAINER_NAME" >/dev/null 2>&1; then
  docker rm -f "$PREVIOUS_NAME" >/dev/null 2>&1 || true
  docker stop "$CONTAINER_NAME"
  docker rename "$CONTAINER_NAME" "$PREVIOUS_NAME"
fi

# 새 컨테이너 실행
if ! docker run -d \
  --name "$CONTAINER_NAME" \
  --restart unless-stopped \
  --env-file "$ENV_FILE" \
  -p 8080:8080 \
  "$IMAGE_URI"; then
  echo "Failed to start new container." >&2
  if docker inspect "$PREVIOUS_NAME" >/dev/null 2>&1; then
    docker rename "$PREVIOUS_NAME" "$CONTAINER_NAME"
    docker start "$CONTAINER_NAME"
  fi
  exit 1
fi

# 최대 약 2분 동안 로그인 페이지 응답 확인
for attempt in $(seq 1 24); do
  if curl -fsS "$HEALTH_URL" >/dev/null; then
    echo "Application health check passed."
    docker rm -f "$PREVIOUS_NAME" >/dev/null 2>&1 || true
    exit 0
  fi

  sleep 5
done

echo "Health check failed. Rolling back." >&2
docker logs --tail 100 "$CONTAINER_NAME" || true
docker rm -f "$CONTAINER_NAME" || true

if docker inspect "$PREVIOUS_NAME" >/dev/null 2>&1; then
  docker rename "$PREVIOUS_NAME" "$CONTAINER_NAME"
  docker start "$CONTAINER_NAME"
  echo "Previous container restarted."
else
  echo "No previous container available for rollback." >&2
fi

exit 1
