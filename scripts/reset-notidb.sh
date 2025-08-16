#!/bin/bash

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}==========================================${NC}"
echo -e "${BLUE}Noti Service Database Reset Script${NC}"
echo -e "${BLUE}==========================================${NC}"

# Database credentials
DB_HOST="${DB_HOST:-asyncsite-mysql}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-asyncsite_root_2024!}"
DB_NAME="notidb"

echo -e "\n${YELLOW}⚠️  WARNING: This will completely delete and recreate the notidb database!${NC}"
echo -e "${YELLOW}All existing data will be lost.${NC}\n"

# Function to execute MySQL commands
execute_mysql() {
    docker exec asyncsite-mysql mysql -u${DB_USER} -p${DB_PASSWORD} -e "$1" 2>/dev/null
}

# Step 1: Stop noti-service to prevent connections
echo -e "${BLUE}Step 1: Stopping noti-service...${NC}"
docker stop asyncsite-noti-service 2>/dev/null || true
docker rm asyncsite-noti-service 2>/dev/null || true
echo -e "${GREEN}✓ Service stopped${NC}"

# Step 2: Drop and recreate database
echo -e "\n${BLUE}Step 2: Dropping existing database...${NC}"
execute_mysql "DROP DATABASE IF EXISTS ${DB_NAME};"
echo -e "${GREEN}✓ Database dropped${NC}"

echo -e "\n${BLUE}Step 3: Creating new database with UTF-8 charset...${NC}"
execute_mysql "CREATE DATABASE ${DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
echo -e "${GREEN}✓ Database created with utf8mb4 charset${NC}"

# Step 4: Verify charset settings
echo -e "\n${BLUE}Step 4: Verifying charset configuration...${NC}"
execute_mysql "USE ${DB_NAME}; SHOW VARIABLES LIKE 'character_set%';" | grep -E "character_set_(database|server|client|connection|results)"
echo -e "${GREEN}✓ Charset verified${NC}"

# Step 5: Restart noti-service with fresh database
echo -e "\n${BLUE}Step 5: Restarting noti-service...${NC}"
cd ~/deployments/noti-service

# Pull latest image
echo -e "${YELLOW}Pulling latest Docker image...${NC}"
docker pull ghcr.io/asyncsite/noti-service:latest

# Start the service
docker compose up -d

# Wait for service to be healthy
echo -e "\n${BLUE}Waiting for service to initialize (this may take up to 30 seconds)...${NC}"
for i in {1..30}; do
    if curl -f http://localhost:8089/actuator/health >/dev/null 2>&1; then
        echo -e "\n${GREEN}✅ Service is healthy!${NC}"
        break
    fi
    echo -n "."
    sleep 1
done

# Step 6: Check template initialization
echo -e "\n${BLUE}Step 6: Checking template initialization...${NC}"
echo -e "${YELLOW}Recent logs:${NC}"
docker logs asyncsite-noti-service --tail 50 2>&1 | grep -E "시스템 템플릿|template|Template" | tail -20

# Step 7: Verify templates in database
echo -e "\n${BLUE}Step 7: Verifying templates in database...${NC}"
TEMPLATE_COUNT=$(execute_mysql "USE ${DB_NAME}; SELECT COUNT(*) FROM notification_template;" | tail -1)
echo -e "Templates in database: ${GREEN}${TEMPLATE_COUNT}${NC}"

# Check for charset issues
echo -e "\n${BLUE}Checking for charset corruption...${NC}"
CORRUPTED=$(execute_mysql "USE ${DB_NAME}; SELECT COUNT(*) FROM notification_template WHERE content_template LIKE '%???%';" | tail -1)
if [ "$CORRUPTED" = "0" ]; then
    echo -e "${GREEN}✓ No charset corruption detected${NC}"
else
    echo -e "${RED}⚠️  Warning: ${CORRUPTED} templates may have charset issues${NC}"
fi

# Step 8: Test email sending
echo -e "\n${BLUE}Step 8: Testing email notification...${NC}"
echo -e "${YELLOW}Sending test email...${NC}"

TEST_RESPONSE=$(curl -s -X POST http://localhost:8089/api/noti \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test@example.com",
    "channelType": "EMAIL",
    "eventType": "ACTION",
    "templateId": "passkey-otp",
    "variables": {
      "code": "123456",
      "expiryMinutes": "5"
    },
    "recipientContact": "test@example.com"
  }' 2>/dev/null)

if echo "$TEST_RESPONSE" | grep -q "success"; then
    echo -e "${GREEN}✅ Test email sent successfully!${NC}"
else
    echo -e "${RED}❌ Test email failed. Response:${NC}"
    echo "$TEST_RESPONSE"
fi

echo -e "\n${BLUE}==========================================${NC}"
echo -e "${GREEN}Database reset complete!${NC}"
echo -e "${BLUE}==========================================${NC}"

echo -e "\n${YELLOW}Next steps:${NC}"
echo "1. Check the logs: docker logs asyncsite-noti-service --tail 100"
echo "2. Verify templates: docker exec asyncsite-mysql mysql -uroot -p${DB_PASSWORD} -e 'USE notidb; SELECT template_id FROM notification_template;'"
echo "3. Test actual email sending from the application"