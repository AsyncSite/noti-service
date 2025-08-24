package com.asyncsite.notiservice.adapter.in.event;

import com.asyncsite.notiservice.adapter.in.event.dto.NotificationEvent;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import com.asyncsite.notiservice.domain.port.in.NotificationUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationUseCase notificationUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topics.notification:asyncsite.noti}",
            groupId = "${spring.application.name}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void on(
            @Payload JsonNode eventNode,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = "correlationId", required = false) String correlationId,
            Acknowledgment acknowledgment) throws JsonProcessingException {
        NotificationEvent event = objectMapper.treeToValue(eventNode, NotificationEvent.class);

        log.info("[KAFKA] Received PasskeyOtpRequestedEvent - EventId: {}, User: {}, Topic: {}, Partition: {}, Offset: {}, CorrelationId: {}",
                event.getEventId(), event.getUserId(), topic, partition, offset, correlationId);

        notificationUseCase.createNotificationBulk(
                event.getUserId(),
                ChannelType.valueOf(event.getChannelType()),
                EventType.valueOf(event.getEventType()),
                event.getData(),
                event.getRecipientContacts()
        );

        // Acknowledge the message
        if (acknowledgment != null) {
            acknowledgment.acknowledge();
        }
    }
}
