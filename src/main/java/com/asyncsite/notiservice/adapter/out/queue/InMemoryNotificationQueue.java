package com.asyncsite.notiservice.adapter.out.queue;

import com.asyncsite.notiservice.domain.model.command.NotificationCommand;
import com.asyncsite.notiservice.domain.port.out.NotificationQueuePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of NotificationQueuePort using Spring Events
 * This can be replaced with Kafka/RabbitMQ in production
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryNotificationQueue implements NotificationQueuePort {

    private final ApplicationEventPublisher eventPublisher;
    private final TaskScheduler taskScheduler;

    // Track failed messages for DLQ
    private final Map<String, Integer> failureCount = new ConcurrentHashMap<>();
    private static final int MAX_RETRY_COUNT = 3;

    @Override
    public void send(NotificationCommand command) {
        log.debug("Publishing notification command: {}", command.notificationId());
        eventPublisher.publishEvent(new NotificationCommandEvent(command));
    }

    @Override
    public void sendDelayed(NotificationCommand command, Duration delay) {
        log.debug("Scheduling delayed notification command: {} with delay: {}",
            command.notificationId(), delay);

        taskScheduler.schedule(
            () -> send(command),
            Instant.now().plus(delay)
        );
    }

    @Override
    public void sendToDLQ(NotificationCommand command, Exception error) {
        String notificationId = command.notificationId();
        int currentFailures = failureCount.merge(notificationId, 1, Integer::sum);

        log.error("Sending notification {} to DLQ. Failure count: {}, Error: {}",
            notificationId, currentFailures, error.getMessage(), error);

        if (currentFailures < MAX_RETRY_COUNT) {
            // Retry with exponential backoff
            Duration backoff = Duration.ofSeconds((long) Math.pow(2, currentFailures));
            NotificationCommand retryCommand = NotificationCommand.createRetryCommand(
                notificationId,
                currentFailures
            );
            sendDelayed(retryCommand, backoff);
        } else {
            // Max retries exceeded, log and move to permanent failure state
            log.error("Notification {} exceeded max retry count. Moving to permanent failure.",
                notificationId);
            failureCount.remove(notificationId);
            // In production, this would persist to a DLQ table or external queue
            eventPublisher.publishEvent(new NotificationFailedEvent(notificationId, error));
        }
    }

    /**
     * Clear failure count for successful notification
     */
    public void clearFailureCount(String notificationId) {
        failureCount.remove(notificationId);
    }
}