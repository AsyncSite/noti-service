#!/bin/bash

# 환경 변수
ENVIRONMENT=${1:-local}
MAX_ATTEMPTS=30
SLEEP_TIME=2

# 색상 정의
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 환경별 헬스체크 URL 설정
case $ENVIRONMENT in
    local)
        HEALTH_URL="http://localhost:8089/actuator/health"
        ;;
    staging)
        HEALTH_URL="http://noti-staging.asyncsite.com:8089/actuator/health"
        ;;
    production)
        HEALTH_URL="http://noti.asyncsite.com:8089/actuator/health"
        ;;
    *)
        HEALTH_URL="http://localhost:8089/actuator/health"
        ;;
esac

echo "🏥 Performing health check on $ENVIRONMENT environment..."
echo "   URL: $HEALTH_URL"

# 헬스체크 수행
attempt=1
while [ $attempt -le $MAX_ATTEMPTS ]; do
    echo -n "   Attempt $attempt/$MAX_ATTEMPTS... "
    
    # curl로 헬스체크
    if curl -f -s "$HEALTH_URL" > /dev/null; then
        echo -e "${GREEN}✓ Healthy${NC}"
        
        # 상세 헬스 정보 출력
        echo ""
        echo "📊 Health Status:"
        curl -s "$HEALTH_URL" | python3 -m json.tool 2>/dev/null || curl -s "$HEALTH_URL"
        
        echo ""
        echo -e "${GREEN}✅ Noti Service is healthy!${NC}"
        exit 0
    else
        echo -e "${YELLOW}⏳ Not ready yet${NC}"
        
        if [ $attempt -eq $MAX_ATTEMPTS ]; then
            echo ""
            echo -e "${RED}❌ Health check failed after $MAX_ATTEMPTS attempts${NC}"
            echo "   Please check the service logs:"
            echo "   docker logs asyncsite-noti-service"
            exit 1
        fi
        
        sleep $SLEEP_TIME
        attempt=$((attempt + 1))
    fi
done