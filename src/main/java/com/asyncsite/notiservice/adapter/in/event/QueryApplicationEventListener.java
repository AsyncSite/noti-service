package com.asyncsite.notiservice.adapter.in.event;

import com.asyncsite.notiservice.adapter.in.event.dto.QueryApplicationCreatedEvent;
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

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Kafka listener for QueryDaily application events.
 * Sends email confirmations when users submit beta test applications.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueryApplicationEventListener {

    private final NotificationUseCase notificationUseCase;
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @KafkaListener(
        topics = "${kafka.topics.querydaily-application:asyncsite.querydaily.application.created}",
        groupId = "${spring.application.name}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleQueryApplicationCreated(
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
            QueryApplicationCreatedEvent event = objectMapper.treeToValue(eventNode, QueryApplicationCreatedEvent.class);

            log.info("[KAFKA] Received QueryApplicationCreatedEvent - QueryId: {}, Email: {}, Name: {}, Topic: {}, Partition: {}, Offset: {}, CorrelationId: {}",
                    event.getQueryId(), event.getEmail(), event.getName(), topic, partition, offset, correlationId);

            // Build metadata for notification
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("templateId", "querydaily-application-confirmation");

            // Build template variables for the email
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", event.getName());
            variables.put("userEmail", event.getEmail());
            variables.put("applicationDate", event.getCreatedAt() != null
                ? event.getCreatedAt().format(DATE_FORMATTER)
                : "");
            variables.put("resumeFileName", event.getResumeFileName());
            variables.put("queryId", String.valueOf(event.getQueryId()));

            metadata.put("variables", variables);

            // Additional context
            if (correlationId != null) {
                metadata.put("correlationId", correlationId);
            }
            metadata.put("queryId", event.getQueryId());

            // Create notification for email delivery
            // Since this is for external users (beta applicants), we pass email as recipientContact
            notificationUseCase.createNotification(
                null, // No userId for beta applicants
                ChannelType.EMAIL,
                EventType.NOTI,
                metadata,
                event.getEmail()
            );

            log.info("[KAFKA] Successfully processed QueryApplicationCreatedEvent for QueryId: {} and email: {}",
                    event.getQueryId(), event.getEmail());

            // Acknowledge the message
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

        } catch (Exception e) {
            log.error("[KAFKA] Failed to process QueryApplicationCreatedEvent - Error: {}",
                    e.getMessage(), e);

            // Don't acknowledge on error - message will be retried
            // Kafka will handle retry based on consumer configuration
            throw new RuntimeException("Failed to process QueryDaily application event", e);
        } finally {
            MDC.remove("correlationId");
        }
    }
}