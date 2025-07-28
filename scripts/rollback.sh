#!/bin/bash
set -e

# 색상 정의
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "🔄 Rolling back Noti Service..."

# 현재 실행 중인 이미지 태그 확인
CURRENT_TAG=$(docker inspect asyncsite-noti-service --format='{{.Config.Image}}' | cut -d: -f2)
echo -e "${YELLOW}Current version: $CURRENT_TAG${NC}"

# 이전 버전 목록 확인
echo "📋 Available versions:"
docker images asyncsite/noti-service --format "table {{.Tag}}\t{{.CreatedAt}}" | head -10

# 롤백할 버전 입력
read -p "Enter the version to rollback to (or 'cancel' to abort): " ROLLBACK_VERSION

if [ "$ROLLBACK_VERSION" == "cancel" ]; then
    echo -e "${YELLOW}Rollback cancelled${NC}"
    exit 0
fi

# 해당 버전 이미지 존재 확인
if ! docker images asyncsite/noti-service:$ROLLBACK_VERSION --format "{{.Tag}}" | grep -q "$ROLLBACK_VERSION"; then
    echo -e "${RED}❌ Version $ROLLBACK_VERSION not found locally${NC}"
    echo "Trying to pull from registry..."
    
    if docker pull asyncsite/noti-service:$ROLLBACK_VERSION; then
        echo -e "${GREEN}✓ Image pulled successfully${NC}"
    else
        echo -e "${RED}❌ Failed to pull image${NC}"
        exit 1
    fi
fi

# 백업 태그 생성
echo "💾 Creating backup of current version..."
docker tag asyncsite/noti-service:$CURRENT_TAG asyncsite/noti-service:backup-$(date +%Y%m%d-%H%M%S)

# 컨테이너 정지
echo "⏹️  Stopping current container..."
docker-compose -f docker-compose.noti-only.yml down

# 새 버전으로 시작
echo "▶️  Starting with version $ROLLBACK_VERSION..."
IMAGE_TAG=$ROLLBACK_VERSION docker-compose -f docker-compose.noti-only.yml up -d

# 헬스체크
echo "⏳ Waiting for service to be healthy..."
./scripts/health-check.sh

echo -e "${GREEN}✅ Rollback to version $ROLLBACK_VERSION completed!${NC}"
echo ""
echo "If you need to rollback this rollback, the previous version is tagged as:"
echo "  asyncsite/noti-service:backup-$(date +%Y%m%d-%H%M%S)"