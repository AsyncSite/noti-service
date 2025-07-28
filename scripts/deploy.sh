#!/bin/bash
set -e

# 환경 변수
ENVIRONMENT=${1:-staging}
IMAGE_TAG=${2:-latest}

echo "🚀 Deploying Noti Service to $ENVIRONMENT with tag $IMAGE_TAG..."

# 색상 정의
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 환경별 설정
case $ENVIRONMENT in
    local)
        export SPRING_PROFILES_ACTIVE="local"
        export SERVER_PORT="8089"
        export EUREKA_SERVER_URL="http://localhost:8761/eureka/"
        export DB_HOST="localhost"
        ;;
    staging)
        export SPRING_PROFILES_ACTIVE="staging"
        export SERVER_PORT="8089"
        export EUREKA_SERVER_URL="http://eureka-staging.asyncsite.com:8761/eureka/"
        export DB_HOST="mysql-staging.asyncsite.com"
        ;;
    production)
        export SPRING_PROFILES_ACTIVE="production"
        export SERVER_PORT="8089"
        export EUREKA_SERVER_URL="http://eureka.asyncsite.com:8761/eureka/"
        export DB_HOST="mysql.asyncsite.com"
        ;;
    *)
        echo -e "${RED}Unknown environment: $ENVIRONMENT${NC}"
        echo "Usage: ./deploy.sh [local|staging|production] [image-tag]"
        exit 1
        ;;
esac

echo -e "${YELLOW}Environment: $ENVIRONMENT${NC}"
echo -e "${YELLOW}Profile: $SPRING_PROFILES_ACTIVE${NC}"
echo -e "${YELLOW}Image Tag: $IMAGE_TAG${NC}"

# Docker Compose 파일 선택
if [ "$ENVIRONMENT" == "local" ]; then
    COMPOSE_FILE="docker-compose.yml"
else
    COMPOSE_FILE="docker-compose.noti-only.yml"
fi

# 기존 컨테이너 정지
echo "⏹️  Stopping existing containers..."
docker-compose -f $COMPOSE_FILE down || true

# 새 이미지로 업데이트
if [ "$ENVIRONMENT" != "local" ]; then
    echo "📥 Pulling latest image..."
    docker pull asyncsite/noti-service:$IMAGE_TAG
fi

# 컨테이너 시작
echo "▶️  Starting containers..."
IMAGE_TAG=$IMAGE_TAG docker-compose -f $COMPOSE_FILE up -d

# 헬스체크 대기
echo "⏳ Waiting for service to be healthy..."
./scripts/health-check.sh $ENVIRONMENT

echo -e "${GREEN}✅ Deployment to $ENVIRONMENT completed!${NC}"
echo "📊 View logs: docker logs -f asyncsite-noti-service"
echo "🔍 Check status: docker ps | grep noti-service"