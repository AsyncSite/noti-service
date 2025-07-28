# Noti Service - Logging Configuration

## Overview
Noti Service uses Logback for structured logging with JSON format support for ELK (Elasticsearch, Logstash, Kibana) integration.

## Logging Configuration

### 1. Logback Configuration
The service uses different logging configurations based on the Spring profile:

- **local/dev**: Console output with standard pattern
- **docker**: JSON format for both console and file output
- **staging/prod**: JSON format with async appenders for performance

Configuration file: `src/main/resources/logback-spring.xml`

### 2. Log Output
- **Console**: JSON format in Docker/Production environments
- **File**: `/app/logs/application.log` with daily rotation
- **Max file size**: 100MB per file
- **Retention**: 7 days (Docker), 30 days (Production)

### 3. Log Levels
Default log levels by profile:
- **local/dev**: DEBUG for application code
- **docker/prod**: INFO for application, WARN for frameworks

## ELK Integration

### 1. Filebeat Configuration
Filebeat collects logs from the application and forwards them to Logstash.

Configuration file: `filebeat-noti-service.yml`
- Input: JSON logs from `/app/logs/*.log`
- Output: Logstash on port 5044

### 2. Running with Filebeat
```bash
# Start noti-service with Filebeat
./gradlew dockerUpWithFilebeat

# View logs
./gradlew dockerLogsWithFilebeat

# Stop services
./gradlew dockerDownWithFilebeat
```

### 3. Docker Compose with Filebeat
The `docker-compose.with-filebeat.yml` file includes:
- Noti Service container
- Filebeat sidecar container
- Shared volume for log files

## Log Fields
JSON logs include the following fields:
- `@timestamp`: Log timestamp
- `level`: Log level (ERROR, WARN, INFO, DEBUG)
- `logger`: Logger name
- `thread`: Thread name
- `message`: Log message
- `service`: Service name (noti-service)
- `environment`: Environment (docker, prod, etc.)
- `mdc`: Mapped Diagnostic Context (correlation IDs, etc.)

## Viewing Logs

### Local Development
```bash
# Console logs
./gradlew bootRun

# Docker logs
docker logs asyncsite-noti-service -f
```

### Production
Logs are collected by Filebeat and sent to the ELK stack. Access Kibana to view and search logs.

### Log Search in Kibana
Example queries:
- Service logs: `service:"noti-service"`
- Error logs: `level:"ERROR" AND service:"noti-service"`
- Specific user: `mdc.userId:"12345"`

## Troubleshooting

### No logs appearing
1. Check if the service is running: `docker ps`
2. Check Filebeat status: `docker logs asyncsite-filebeat-noti-service`
3. Verify log volume is mounted: `docker inspect asyncsite-noti-service`

### Log format issues
1. Ensure `logstash-logback-encoder` dependency is included
2. Verify Spring profile is set correctly
3. Check logback-spring.xml is in the classpath

### Performance issues
1. Adjust async appender queue size in logback-spring.xml
2. Increase Filebeat resources if needed
3. Consider adjusting log levels to reduce volume