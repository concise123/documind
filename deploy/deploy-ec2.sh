
#!/usr/bin/env bash
set -Eeuo pipefail

IMAGE_URI="${1:?Usage: deploy-ec2.sh IMAGE_URI AWS_REGION}"
AWS_REGION="${2:?Usage: deploy-ec2.sh IMAGE_URI AWS_REGION}"

CONTAINER_NAME="documind"
PREVIOUS_NAME="documind-previous"
ENV_FILE="/home/ubuntu/documind.env"
HEALTH_URL="http://127.0.0.1:8080/login"

echo "Deploying image: ${IMAGE_URI}"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Environment file not found: $ENV_FILE"
  exit 1
fi

# ECR 로그인
REGISTRY="${IMAGE_URI%%/*}"

aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "$REGISTRY"

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
  echo "Failed to start new container."
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

echo "Health check failed. Rolling back."
docker logs --tail 100 "$CONTAINER_NAME" || true
docker rm -f "$CONTAINER_NAME" || true

if docker inspect "$PREVIOUS_NAME" >/dev/null 2>&1; then
  docker rename "$PREVIOUS_NAME" "$CONTAINER_NAME"
  docker start "$CONTAINER_NAME"
  echo "Previous container restarted."
else
  echo "No previous container available for rollback."
fi

exit 1
