# Kafka Event Consumer Documentation for Noti Service

## Overview
This document describes the Kafka event consumer implementation in noti-service, focusing on receiving and processing Passkey OTP events from user-service to send email notifications.

## Architecture

### Event Flow
```
user-service → Kafka (asyncsite.passkey.otp) → noti-service → Email Provider
```

1. **user-service publishes** PasskeyOtpRequestedEvent to Kafka
2. **noti-service consumes** the event from topic
3. **Email is sent** to user with OTP code
4. **Manual acknowledgment** confirms processing

## Implementation Details

### 1. Kafka Configuration

#### KafkaConfig.java
Located at: `config/KafkaConfig.java`

```java
@Configuration
@EnableKafka
public class KafkaConfig {
    
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "noti-service");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        
        // CRITICAL: Type mappings for cross-service deserialization
        config.put(JsonDeserializer.TYPE_MAPPINGS, 
            "com.asyncsite.userservice.auth.domain.event.PasskeyOtpRequestedEvent:" +
            "com.asyncsite.notiservice.adapter.in.event.dto.PasskeyOtpRequestedEvent");
            
        return new DefaultKafkaConsumerFactory<>(config);
    }
}
```

**Key Points:**
- ✅ Manual acknowledgment (`ENABLE_AUTO_COMMIT_CONFIG = false`)
- ✅ TYPE_MAPPINGS for deserializing events from other services
- ✅ Consumer group ID: `noti-service`
- ⚠️ TYPE_MAPPINGS is NOT a workaround - it's REQUIRED for cross-service communication

### 2. Event Consumer

#### PasskeyOtpEventConsumer.java
Located at: `adapter/in/event/PasskeyOtpEventConsumer.java`

```java
@Component
@Slf4j
public class PasskeyOtpEventConsumer {
    
    @KafkaListener(
        topics = "asyncsite.passkey.otp",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
        @Payload PasskeyOtpRequestedEvent event,
        @Header(value = "correlationId", required = false) String correlationId,
        Acknowledgment acknowledgment
    ) {
        MDC.put("correlationId", correlationId);
        try {
            log.info("[KAFKA] Received PasskeyOtpRequestedEvent - EventId: {}, User: {}", 
                event.getEventId(), event.getEmail());
            
            // Process event
            sendPasskeyOtpEmail(event);
            
            // Acknowledge only after successful processing
            acknowledgment.acknowledge();
            log.info("[KAFKA] Successfully processed PasskeyOtpRequestedEvent for user: {}", 
                event.getEmail());
        } finally {
            MDC.clear();
        }
    }
}
```

### 3. Event DTO

#### PasskeyOtpRequestedEvent.java
Located at: `adapter/in/event/dto/PasskeyOtpRequestedEvent.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasskeyOtpRequestedEvent {
    private String eventId;
    private String userId;
    private String email;
    private String otpCode;
    private Long expiryMinutes;
    private String clientIp;
    private LocalDateTime eventTime;
}
```

**Important:**
- Field names must match the producer's event
- No inheritance required (BaseEvent is producer-side only)
- Jackson handles deserialization via TYPE_MAPPINGS

### 4. Topic Creation

#### docker-compose.yml
```yaml
kafka-init:
  image: bitnami/kafka:3.7
  command: |
    kafka-topics.sh --create --if-not-exists \
      --bootstrap-server asyncsite-kafka:9092 \
      --topic asyncsite.passkey.otp \
      --partitions 3 \
      --replication-factor 1
```

**Topic Ownership Principle:**
- ✅ Consumer (noti-service) creates the topic
- ❌ Producer (user-service) should NOT create consumer topics

## Configuration

### application.yml
```yaml
# Kafka configuration
kafka:
  bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:asyncsite-kafka:9092}
  consumer:
    group-id: noti-service
    auto-offset-reset: earliest
    enable-auto-commit: false  # Manual acknowledgment

# Event processing
event:
  enabled: true  # Must be true to receive events

spring:
  kafka:
    consumer:
      properties:
        spring.json.trusted.packages: "*"
```

**Configuration Notes:**
- ✅ Use application.yml for configuration (not Docker env vars)
- ✅ Default to `asyncsite-kafka:9092` for Docker networking
- ✅ Manual acknowledgment for reliability

## Testing Guide

### Prerequisites
```bash
# 1. Verify Kafka is running
docker ps | grep kafka

# 2. Check topic exists
docker exec asyncsite-kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list | grep passkey
```

### Testing Event Consumption

#### 1. Monitor noti-service logs
```bash
docker logs -f asyncsite-noti-service | grep -E "KAFKA|Received|CorrelationId"
```

#### 2. Trigger event from user-service
```bash
# Must use Gateway for proper correlation ID
curl -X POST http://localhost:8080/api/webauthn/otp/start \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'
```

#### 3. Verify consumption
Expected logs:
```
[KAFKA] Received PasskeyOtpRequestedEvent - EventId: xxx, User: test@example.com
Processing passkey OTP email for: test@example.com
Email sent successfully to: test@example.com
[KAFKA] Successfully processed PasskeyOtpRequestedEvent for user: test@example.com
```

### Debugging Consumer Issues

#### Check Consumer Group Status
```bash
docker exec asyncsite-kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group noti-service \
  --describe
```

Output shows:
- TOPIC: asyncsite.passkey.otp
- CURRENT-OFFSET: Messages consumed
- LOG-END-OFFSET: Total messages
- LAG: Unprocessed messages

#### View Messages in Topic
```bash
docker exec asyncsite-kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic asyncsite.passkey.otp \
  --from-beginning \
  --max-messages 5
```

## Troubleshooting

### Issue: "No type information in headers"
**Symptom:**
```
Cannot deserialize value of type `PasskeyOtpRequestedEvent`
No type information in headers and no default type provided
```

**Cause:** Missing TYPE_MAPPINGS configuration

**Solution:**
```java
config.put(JsonDeserializer.TYPE_MAPPINGS, 
    "com.asyncsite.userservice.auth.domain.event.PasskeyOtpRequestedEvent:" +
    "com.asyncsite.notiservice.adapter.in.event.dto.PasskeyOtpRequestedEvent");
```

### Issue: Kafka Connection Failed
**Symptom:**
```
Connection to node -1 (localhost/127.0.0.1:9092) could not be established
```

**Cause:** Wrong bootstrap servers configuration

**Solution:**
1. Update application.yml:
```yaml
kafka:
  bootstrap-servers: asyncsite-kafka:9092  # NOT localhost:9092
```

2. Restart service:
```bash
./scripts/dockerRebuildAndRunNotiOnly.sh
```

### Issue: Consumer Not Joining Group
**Symptom:** No consumption logs despite events being published

**Check:**
```bash
# List consumer groups
docker exec asyncsite-kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --list

# Should see "noti-service" in the list
```

**Solution:**
1. Verify @KafkaListener annotation present
2. Check event.enabled = true
3. Restart noti-service

### Issue: Duplicate Messages
**Symptom:** Same event processed multiple times

**Cause:** Auto-commit enabled or acknowledgment not called

**Solution:**
```java
// Ensure manual acknowledgment
config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

// Always acknowledge after successful processing
acknowledgment.acknowledge();
```

### Issue: Missing Correlation ID
**Symptom:** CorrelationId is null in logs

**Check:**
```bash
docker logs asyncsite-noti-service | grep "CorrelationId"
```

**Solution:**
1. Ensure requests go through Gateway (port 8080)
2. Verify header extraction in consumer:
```java
@Header(value = "correlationId", required = false) String correlationId
```

## Best Practices

### 1. Consumer Configuration
- Always use manual acknowledgment for reliability
- Set appropriate consumer group ID
- Configure TYPE_MAPPINGS for cross-service events
- Use application.yml for configuration (not env vars)

### 2. Error Handling
```java
try {
    // Process event
    sendEmail(event);
    
    // Acknowledge only on success
    acknowledgment.acknowledge();
} catch (Exception e) {
    log.error("Failed to process event", e);
    // Don't acknowledge - message will be redelivered
    throw e;  // Let Kafka retry
}
```

### 3. Monitoring
- Log with clear prefixes: `[KAFKA]`, `[EMAIL]`
- Include correlation ID in all logs
- Monitor consumer lag regularly
- Set up alerts for processing failures

### 4. Topic Management
- Consumer owns and creates topics
- Use meaningful topic names with namespaces
- Configure appropriate partitions for scale
- Set retention policies based on requirements

## Common Mistakes to Avoid

### ❌ Using localhost:9092 in Docker
```yaml
# Wrong
kafka:
  bootstrap-servers: localhost:9092
```

### ✅ Use Docker service name
```yaml
# Correct
kafka:
  bootstrap-servers: asyncsite-kafka:9092
```

### ❌ Auto-commit enabled
```java
// Wrong - may lose messages
config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
```

### ✅ Manual acknowledgment
```java
// Correct - ensures processing
config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
```

### ❌ Missing TYPE_MAPPINGS
```java
// Wrong - deserialization will fail
// No TYPE_MAPPINGS configuration
```

### ✅ Configure TYPE_MAPPINGS
```java
// Correct - enables cross-service events
config.put(JsonDeserializer.TYPE_MAPPINGS, "mapping:here");
```

### ❌ Testing directly on service port
```bash
# Wrong - bypasses Gateway
curl http://localhost:8082/api/noti/...
```

### ✅ Always use Gateway
```bash
# Correct - includes correlation ID
curl http://localhost:8080/api/noti/...
```

## Performance Tuning

### Consumer Settings
```yaml
spring:
  kafka:
    consumer:
      max-poll-records: 500  # Batch size
      fetch-min-size: 1024   # Min bytes per fetch
      fetch-max-wait: 500ms  # Max wait time
```

### Concurrency
```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, Object> 
       kafkaListenerContainerFactory() {
    factory.setConcurrency(3);  // Parallel consumers
    return factory;
}
```

## Related Documentation
- [User Service Event Publishing](../../user-service/docs/EVENT_DRIVEN_ARCHITECTURE.md)
- [Core Platform Kafka Guide](../../core-platform/docs/kafka/KAFKA_INTEGRATION.md)
- [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/reference/)

## Version History
- 2025-08-17: Initial Kafka consumer implementation
- 2025-08-17: Fixed localhost to asyncsite-kafka configuration
- 2025-08-17: Added TYPE_MAPPINGS for cross-service events