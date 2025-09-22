# Optimistic Locking Issue - Solution Implementation

## Problem Summary
The noti-service was experiencing optimistic locking exceptions when sending notifications. The root cause was that the notification entity was being saved twice with a version mismatch:
1. First save in `NotificationService`
2. Second save in `NotiEventHandler` (async, different transaction)

## Solution: Command Pattern with In-Memory Queue

### Architecture Changes

#### 1. Created Command Model
- **File**: `domain/model/command/NotificationCommand.java`
- **Purpose**: Lightweight command object containing only notification ID
- **Benefit**: Avoids passing full entity between transactions

#### 2. Introduced Queue Abstraction
- **File**: `domain/port/out/NotificationQueuePort.java`
- **Purpose**: Abstract interface for queue operations
- **Benefit**: Can swap in-memory implementation for Kafka/RabbitMQ later

#### 3. In-Memory Queue Implementation
- **File**: `adapter/out/queue/InMemoryNotificationQueue.java`
- **Features**:
  - Uses Spring's `ApplicationEventPublisher`
  - Supports delayed sending with `TaskScheduler`
  - Includes DLQ (Dead Letter Queue) with exponential backoff
  - Max retry count: 3

#### 4. Command Handler
- **File**: `adapter/in/event/NotificationCommandHandler.java`
- **Features**:
  - Listens for `NotificationCommandEvent`
  - Fetches fresh entity from DB (avoids version conflict)
  - Handles retries and failures gracefully
  - Updates status after successful send

#### 5. Modified NotificationService
- **Change**: Now publishes command with ID only
- **Before**: `notiEventSender.notiCreated(notification)`
- **After**: `notificationQueue.send(NotificationCommand.createSendCommand(notification.getNotificationId()))`

### Disabled Components
- `NotiEventHandler.java` - Commented out `@Component`
- `NotiEventSenderImpl.java` - Commented out `@Component`

### Test Results
- All 57 tests passing ✅
- No compilation errors
- Docker build successful
- Service running healthy

### Benefits of This Approach

1. **No More Optimistic Locking**: Entity is fetched fresh in each transaction
2. **Scalable Architecture**: Easy to replace with real message queue
3. **Retry Logic**: Built-in exponential backoff for failures
4. **Clean Separation**: Command pattern separates concerns
5. **Testable**: All components are properly abstracted

### Future Migration Path

When ready to use real message queue (Kafka/RabbitMQ):
1. Create new implementation of `NotificationQueuePort`
2. Update Spring configuration to use new implementation
3. No changes needed in domain or application layers

### Key Files Changed

| File | Type | Description |
|------|------|-------------|
| NotificationCommand.java | NEW | Command model with notification ID |
| NotificationQueuePort.java | NEW | Queue abstraction interface |
| InMemoryNotificationQueue.java | NEW | In-memory queue implementation |
| NotificationCommandHandler.java | NEW | Async command handler |
| NotificationCommandEvent.java | NEW | Spring event wrapper |
| NotificationFailedEvent.java | NEW | Failed notification event |
| SchedulerConfig.java | NEW | TaskScheduler configuration |
| NotificationService.java | MODIFIED | Uses queue instead of event sender |
| NotiEventHandler.java | DISABLED | Commented out @Component |
| NotiEventSenderImpl.java | DISABLED | Commented out @Component |

## Verification

Run the following to verify the fix:
```bash
# Run tests
./gradlew test

# Build and run with Docker
./gradlew dockerRebuildAndRunNotiOnly

# Check health
curl http://localhost:8089/actuator/health
```

## Monitoring

To monitor for optimistic locking exceptions:
```bash
docker logs asyncsite-noti-service -f | grep -i "optimistic"
```

The issue should now be completely resolved.