
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

# 인증 정보를 저장할 임시 디렉터리 생성
AUTH_DIR="$(mktemp -d)"
chmod 700 "$AUTH_DIR"

# 스크립트 종료 시 임시 인증 정보 삭제
cleanup() {
  rm -rf "$AUTH_DIR"
}
trap cleanup EXIT

# Docker와 Cosign이 동일한 임시 인증 설정을 사용하도록 지정
export DOCKER_CONFIG="$AUTH_DIR"

# 임시 Docker 설정에 ECR 인증 정보 저장
aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "$ECR_REGISTRY"

# Cosign 서명 검증
cosign verify \
  --certificate-identity="$EXPECTED_IDENTITY" \
  --certificate-oidc-issuer="$EXPECTED_ISSUER" \
  "$IMAGE_URI"

echo "Image signature verified."

# 새 이미지 다운로드
docker pull "$IMAGE_URI"

# 헬스 체크: 최대 약 2분 동안 응답 확인
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

# 이전 컨테이너 복구 및 정상 응답 확인
rollback_previous_container() {
  echo "Attempting to restore previous container."

  if ! docker inspect "$PREVIOUS_NAME" >/dev/null 2>&1; then
    echo "No previous container available for rollback." >&2
    return 1
  fi

  if docker inspect "$CONTAINER_NAME" >/dev/null 2>&1; then
    if ! docker rm -f "$CONTAINER_NAME" >/dev/null; then
      echo "Failed to remove failed container." >&2
      return 1
    fi
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

# 현재 컨테이너가 없어도 이전 컨테이너를 롤백 후보로 보존
if ! docker inspect "$CONTAINER_NAME" >/dev/null 2>&1; then
  if docker inspect "$PREVIOUS_NAME" >/dev/null 2>&1; then
    echo "Current container is missing. Previous container retained for rollback."
  fi
fi

# 현재 컨테이너 보존
if docker inspect "$CONTAINER_NAME" >/dev/null 2>&1; then
  # 현재 컨테이너를 먼저 중지
  if ! docker stop "$CONTAINER_NAME"; then
    echo "Failed to stop current container." >&2
    exit 1
  fi

  # 이전 배포의 오래된 백업은 현재 컨테이너가 중지된 뒤 정리
  if docker inspect "$PREVIOUS_NAME" >/dev/null 2>&1; then
    if ! docker rm -f "$PREVIOUS_NAME" >/dev/null; then
      echo "Failed to remove stale previous container." >&2

      if ! docker start "$CONTAINER_NAME"; then
        echo "Failed to restart current container. Manual recovery required." >&2
      fi

      exit 1
    fi
  fi

  # 현재 컨테이너를 롤백용으로 보존
  if ! docker rename "$CONTAINER_NAME" "$PREVIOUS_NAME"; then
    echo "Failed to preserve current container." >&2

    if ! docker start "$CONTAINER_NAME"; then
      echo "Failed to restart current container. Manual recovery required." >&2
    fi

    exit 1
  fi
fi

# 신규 컨테이너 실행
if ! docker run -d \
  --name "$CONTAINER_NAME" \
  --restart unless-stopped \
  --env-file "$ENV_FILE" \
  -p 8080:8080 \
  "$IMAGE_URI"; then
  echo "Failed to start new container." >&2

  if ! rollback_previous_container; then
    echo "Automatic rollback failed. Manual recovery required." >&2
  fi

  exit 1
fi

# 신규 애플리케이션 헬스 체크
if wait_for_health; then
  echo "Application health check passed."

  # 신규 버전이 정상 응답한 뒤 이전 컨테이너 정리
  if docker inspect "$PREVIOUS_NAME" >/dev/null 2>&1; then
    if ! docker rm "$PREVIOUS_NAME" >/dev/null; then
      echo "WARNING: Deployment succeeded, but previous container cleanup failed." >&2
    fi
  fi

  exit 0
fi

# 헬스 체크 실패: 로그 확인 후 롤백
echo "Health check failed. Rolling back." >&2
docker logs --tail 100 "$CONTAINER_NAME" >&2 || true

if ! rollback_previous_container; then
  echo "Automatic rollback failed. Manual recovery required." >&2
fi

exit 1
