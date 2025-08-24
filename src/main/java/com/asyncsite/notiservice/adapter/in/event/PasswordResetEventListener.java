package com.asyncsite.notiservice.adapter.in.event;

import com.asyncsite.notiservice.adapter.in.event.dto.PasswordResetRequestedEvent;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import com.asyncsite.notiservice.domain.port.in.NotificationUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka listener for password reset request events.
 * Consumes events from user-service and creates notifications for email delivery.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetEventListener {
    
    private final NotificationUseCase notificationUseCase;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(
        topics = "${kafka.topics.password-reset:asyncsite.password.reset}",
        groupId = "${spring.application.name}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePasswordResetRequested(
            @Payload JsonNode eventNode,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = "correlationId", required = false) String correlationId,
            Acknowledgment acknowledgment) {
        
        // Set correlation ID in MDC for tracking
        if (correlationId != null) {
            MDC.put("correlationId", correlationId);
        }
        
        try {
            // Parse the event from JsonNode
            PasswordResetRequestedEvent event = objectMapper.treeToValue(eventNode, PasswordResetRequestedEvent.class);
            
            log.info("[KAFKA] Received PasswordResetRequestedEvent - EventId: {}, User: {}, Topic: {}, Partition: {}, Offset: {}, CorrelationId: {}",
                    event.getEventId(), event.getEmail(), topic, partition, offset, correlationId);
            // Build metadata for notification
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("templateId", "password-reset");
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("resetLink", event.getResetLink());
            variables.put("expiryHours", String.valueOf(event.getExpiryHours()));
            variables.put("resetToken", event.getResetToken());
            metadata.put("variables", variables);
            
            // Additional context
            if (correlationId != null) {
                metadata.put("correlationId", correlationId);
            }
            metadata.put("eventId", event.getEventId());
            metadata.put("clientIp", event.getClientIp());
            
            // Create notification using existing NotificationUseCase
            notificationUseCase.createNotification(
                event.getUserId(),
                ChannelType.EMAIL,
                EventType.PASSWORD_RESET,
                metadata,
                event.getEmail()
            );
            
            log.info("[KAFKA] Successfully processed PasswordResetRequestedEvent for user: {}", event.getEmail());
            
            // Acknowledge the message
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            
        } catch (Exception e) {
            log.error("[KAFKA] Failed to process PasswordResetRequestedEvent - Error: {}", 
                    e.getMessage(), e);
            
            // Don't acknowledge on error - message will be retried
            throw new RuntimeException("Failed to process PasswordResetRequestedEvent", e);
        } finally {
            // Clean up MDC after processing
            MDC.remove("correlationId");
        }
    }
}