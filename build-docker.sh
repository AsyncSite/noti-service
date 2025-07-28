#!/bin/bash
set -e

# 색상 정의
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🐳 Noti Service Docker Build Script${NC}"
echo "===================================="

# 버전 정보
VERSION=${1:-latest}
REGISTRY=${DOCKER_REGISTRY:-docker.io}
IMAGE_NAME=${IMAGE_NAME:-asyncsite/noti-service}

echo -e "${YELLOW}Registry: $REGISTRY${NC}"
echo -e "${YELLOW}Image: $IMAGE_NAME:$VERSION${NC}"
echo ""

# 1. Java 버전 확인
echo "☕ Checking Java version..."
java -version

# 2. Gradle clean & build
echo ""
echo "🧹 Cleaning previous build..."
./gradlew clean

echo ""
echo "🔨 Building JAR file..."
if ./gradlew build -x test; then
    echo -e "${GREEN}✓ JAR build successful${NC}"
else
    echo -e "${RED}✗ JAR build failed${NC}"
    exit 1
fi

# 3. JAR 파일 확인
echo ""
echo "📦 Verifying JAR file..."
JAR_FILE=$(find build/libs -name "*.jar" -not -name "*-plain.jar" | head -1)
if [ -f "$JAR_FILE" ]; then
    echo -e "${GREEN}✓ Found JAR: $JAR_FILE${NC}"
    ls -lh "$JAR_FILE"
else
    echo -e "${RED}✗ JAR file not found${NC}"
    exit 1
fi

# 4. Docker 이미지 빌드
echo ""
echo "🐳 Building Docker image..."
if docker build -t $IMAGE_NAME:$VERSION .; then
    echo -e "${GREEN}✓ Docker image built successfully${NC}"
else
    echo -e "${RED}✗ Docker image build failed${NC}"
    exit 1
fi

# 5. 추가 태그 생성
if [ "$VERSION" != "latest" ]; then
    echo ""
    echo "🏷️  Creating additional tags..."
    docker tag $IMAGE_NAME:$VERSION $IMAGE_NAME:latest
    echo -e "${GREEN}✓ Tagged as latest${NC}"
fi

# 6. 이미지 정보 출력
echo ""
echo "📊 Docker image info:"
docker images | grep "$IMAGE_NAME" | head -5

# 7. 옵션: Registry에 푸시
echo ""
read -p "Push to registry? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "📤 Pushing to registry..."
    
    if [ "$VERSION" != "latest" ]; then
        docker push $IMAGE_NAME:$VERSION
        docker push $IMAGE_NAME:latest
    else
        docker push $IMAGE_NAME:latest
    fi
    
    echo -e "${GREEN}✓ Images pushed to registry${NC}"
fi

echo ""
echo -e "${GREEN}✅ Docker build completed!${NC}"
echo ""
echo "To run locally:"
echo "  ./gradlew dockerUpNotiOnly"
echo ""
echo "To run standalone:"
echo "  docker run -d --name noti-service -p 8084:8084 $IMAGE_NAME:$VERSION"