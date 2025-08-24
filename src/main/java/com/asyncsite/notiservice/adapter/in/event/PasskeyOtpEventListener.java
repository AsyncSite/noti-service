package com.asyncsite.notiservice.adapter.in.event;

import com.asyncsite.notiservice.adapter.in.event.dto.PasskeyOtpRequestedEvent;
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
 * Kafka listener for passkey OTP request events.
 * Consumes events from user-service and creates notifications for email delivery.
 * Event consumption is now mandatory - no fallback to REST.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasskeyOtpEventListener {
    
    private final NotificationUseCase notificationUseCase;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(
        topics = "${kafka.topics.passkey-otp:asyncsite.passkey.otp}",
        groupId = "${spring.application.name}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePasskeyOtpRequested(
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
            PasskeyOtpRequestedEvent event = objectMapper.treeToValue(eventNode, PasskeyOtpRequestedEvent.class);
            
            log.info("[KAFKA] Received PasskeyOtpRequestedEvent - EventId: {}, User: {}, Topic: {}, Partition: {}, Offset: {}, CorrelationId: {}",
                    event.getEventId(), event.getEmail(), topic, partition, offset, correlationId);
            // Build metadata for notification
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("templateId", "passkey-otp");
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("code", event.getOtpCode());
            variables.put("expiryMinutes", String.valueOf(event.getExpiryMinutes()));
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
                EventType.ACTION,
                metadata,
                event.getEmail()
            );
            
            log.info("[KAFKA] Successfully processed PasskeyOtpRequestedEvent for user: {}", event.getEmail());
            
            // Acknowledge the message
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            
        } catch (Exception e) {
            log.error("[KAFKA] Failed to process PasskeyOtpRequestedEvent - Error: {}", 
                    e.getMessage(), e);
            
            // Don't acknowledge on error - message will be retried
            // You might want to implement a dead letter queue for persistent failures
            throw new RuntimeException("Failed to process PasskeyOtpRequestedEvent", e);
        } finally {
            // Clean up MDC after processing
            MDC.remove("correlationId");
        }
    }
}