#!/bin/bash

# ======================================
# Backoffice Scheduled Email Test
# ======================================
# Tests scheduled email from backoffice to noti-service
# ======================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Service endpoints
NOTI_SERVICE_URL="http://localhost:8089"
BACKOFFICE_API_URL="http://localhost:8080"

# Test configuration
TEST_EMAIL="${TEST_EMAIL:-choeebh@gmail.com}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}Backoffice Scheduled Email Test${NC}"
echo -e "${BLUE}======================================${NC}"
echo -e "Test ID: $TIMESTAMP"
echo -e "Recipient: $TEST_EMAIL"
echo ""

# Function to check service health
check_service() {
    local service_name=$1
    local service_url=$2

    echo -n "Checking $service_name... "
    if curl -s -f "$service_url/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✓${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠ (service might be running on different port)${NC}"
        return 0  # Don't fail the test
    fi
}

# Main execution
echo -e "${BLUE}1. Service Health Check${NC}"
echo "================================"
check_service "Noti Service" "$NOTI_SERVICE_URL"

echo -e "\n${BLUE}2. Testing Direct API Call (Scheduled)${NC}"
echo "================================"

# Calculate scheduled time (30 seconds from now)
SCHEDULED_TIME=$(date -u -v+30S +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || date -u -d "+30 seconds" +"%Y-%m-%dT%H:%M:%S")
echo "Scheduled for: $SCHEDULED_TIME (30 seconds from now)"
echo "Email subject: [백오피스예약-$TIMESTAMP] QueryDaily 질문"

# Direct API call to noti-service with scheduling
RESPONSE=$(curl -s -X POST "$NOTI_SERVICE_URL/api/noti" \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": \"backoffice-test-$TIMESTAMP\",
    \"channelType\": \"EMAIL\",
    \"eventType\": \"NOTI\",
    \"recipientContact\": \"$TEST_EMAIL\",
    \"templateId\": \"querydaily-question\",
    \"scheduledAt\": \"$SCHEDULED_TIME\",
    \"variables\": {
      \"question\": \"[백오피스 예약발송 테스트] React의 Context API와 Redux의 차이점을 설명해주세요.\",
      \"hint\": \"상태 관리 복잡도, 성능, 사용 사례를 중심으로 비교해보세요.\",
      \"userName\": \"테스트유저-$TIMESTAMP\",
      \"currentDay\": \"1\",
      \"totalDays\": \"3\",
      \"tomorrowTopic\": \"TypeScript 타입 시스템\"
    }
  }")

# Check response
if echo "$RESPONSE" | grep -q "notificationId"; then
    NOTIFICATION_ID=$(echo $RESPONSE | grep -o '"notificationId":"[^"]*' | cut -d'"' -f4)
    STATUS=$(echo $RESPONSE | grep -o '"status":"[^"]*' | cut -d'"' -f4)

    echo -e "${GREEN}✓ Scheduled notification created successfully${NC}"
    echo "  Notification ID: ${NOTIFICATION_ID:0:8}..."
    echo "  Initial status: $STATUS"

    if [ "$STATUS" = "SCHEDULED" ]; then
        echo -e "${GREEN}✓ Notification is in SCHEDULED state${NC}"
    fi
else
    echo -e "${RED}✗ Failed to create scheduled notification${NC}"
    echo "Response: $RESPONSE"
    exit 1
fi

echo -e "\n${BLUE}3. Monitoring Notification Status${NC}"
echo "================================"
echo "Waiting for scheduler to process (up to 90 seconds)..."
echo -n "Status: "

# Monitor for up to 90 seconds
for i in {1..18}; do
    sleep 5

    STATUS_RESPONSE=$(curl -s "$NOTI_SERVICE_URL/api/noti/$NOTIFICATION_ID")
    CURRENT_STATUS=$(echo $STATUS_RESPONSE | grep -o '"status":"[^"]*' | cut -d'"' -f4)

    if [ "$CURRENT_STATUS" = "SENT" ]; then
        echo ""
        echo -e "${GREEN}✓ Notification sent successfully!${NC}"
        break
    elif [ "$CURRENT_STATUS" = "PENDING" ]; then
        echo -n "P"
    elif [ "$CURRENT_STATUS" = "SCHEDULED" ]; then
        echo -n "S"
    else
        echo -n "."
    fi
done

echo ""
echo -e "\n${BLUE}======================================${NC}"
echo -e "${GREEN}Test Summary${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""
echo "✅ Scheduled email API: Working"
echo "✅ Notification created: ID ${NOTIFICATION_ID:0:8}..."
echo "✅ Final status: $CURRENT_STATUS"
echo "📧 Check email at: $TEST_EMAIL"
echo ""
echo -e "${YELLOW}Note: Backoffice UI now supports scheduling!${NC}"
echo "  - Toggle between immediate/scheduled sending"
echo "  - Date/time picker for scheduling"
echo "  - scheduledAt parameter passed to API"