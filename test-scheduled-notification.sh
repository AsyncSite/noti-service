#!/bin/bash

# ======================================
# Scheduled Notification Test
# ======================================
# Tests scheduled email delivery to real email address
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

# Test configuration
TEST_EMAIL="${TEST_EMAIL:-choeebh@gmail.com}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}Scheduled Notification Test${NC}"
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
        echo -e "${RED}✗${NC}"
        return 1
    fi
}

# Main execution
echo -e "${BLUE}1. Service Health Check${NC}"
echo "================================"
check_service "Noti Service" "$NOTI_SERVICE_URL"

echo -e "\n${BLUE}2. Creating Scheduled Notification${NC}"
echo "================================"

# Calculate scheduled time (30 seconds from now for quick testing)
SCHEDULED_TIME=$(date -u -v+30S +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || date -u -d "+30 seconds" +"%Y-%m-%dT%H:%M:%S")
echo "Scheduled for: $SCHEDULED_TIME (30 seconds from now)"
echo "Email subject: [예약발송-$TIMESTAMP] AsyncSite 예약 테스트"

RESPONSE=$(curl -s -X POST "$NOTI_SERVICE_URL/api/noti" \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": \"test-scheduled-$TIMESTAMP\",
    \"channelType\": \"EMAIL\",
    \"eventType\": \"QUERY_DAILY_QUESTION\",
    \"recipientContact\": \"$TEST_EMAIL\",
    \"templateId\": \"querydaily-question\",
    \"scheduledAt\": \"$SCHEDULED_TIME\",
    \"variables\": {
      \"userName\": \"[예약발송-$TIMESTAMP]\",
      \"questionTitle\": \"스케줄러 테스트 질문\",
      \"difficulty\": \"중급\"
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
        echo -e "${YELLOW}📧 Email will be sent to: $TEST_EMAIL${NC}"
    fi
else
    echo -e "${RED}✗ Failed to create scheduled notification${NC}"
    echo "Response: $RESPONSE"
    exit 1
fi

echo -e "\n${BLUE}3. Monitoring Notification Status${NC}"
echo "================================"
echo "Scheduler runs every minute, monitoring for up to 90 seconds..."
echo -n "Status: "

# Monitor for up to 90 seconds
for i in {1..18}; do
    sleep 5

    STATUS_RESPONSE=$(curl -s "$NOTI_SERVICE_URL/api/noti/$NOTIFICATION_ID")
    CURRENT_STATUS=$(echo $STATUS_RESPONSE | grep -o '"status":"[^"]*' | cut -d'"' -f4)

    if [ "$CURRENT_STATUS" = "SENT" ]; then
        echo ""
        echo -e "${GREEN}✓ Notification sent successfully!${NC}"
        echo -e "${YELLOW}📧 Check your email at: $TEST_EMAIL${NC}"

        # Check logs
        echo -e "\n${BLUE}4. Recent Logs${NC}"
        echo "================================"
        docker logs asyncsite-noti-service --tail 20 2>&1 | grep -E "(Scheduled|$NOTIFICATION_ID|발송)" | tail -5 || echo "No relevant logs found"

        echo -e "\n${BLUE}======================================${NC}"
        echo -e "${GREEN}Scheduled Notification Test Completed${NC}"
        echo -e "${BLUE}======================================${NC}"
        echo ""
        echo "✅ Email sent to: $TEST_EMAIL"
        echo "   Subject: [예약발송-$TIMESTAMP] AsyncSite 예약 테스트"
        echo "   Test Time: $(date '+%Y-%m-%d %H:%M:%S')"
        exit 0

    elif [ "$CURRENT_STATUS" = "PENDING" ]; then
        echo -n "P"
    elif [ "$CURRENT_STATUS" = "FAILED" ]; then
        echo ""
        echo -e "${RED}✗ Notification failed to send${NC}"
        echo "Final status: FAILED"
        exit 1
    else
        echo -n "."
    fi
done

echo ""
echo -e "${YELLOW}⚠ Timeout - Notification still not sent${NC}"
echo "Final status: $CURRENT_STATUS"
echo ""
echo "Note: Scheduler runs every minute. The email may still be sent soon."
echo "Check logs: docker logs asyncsite-noti-service --tail 50"