package com.asyncsite.notiservice.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for noti-service.
 * Configures Kafka consumer and producer beans for event-driven communication.
 */
@Slf4j
@Configuration
@EnableKafka
@ConditionalOnProperty(
    name = "event.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class KafkaConfig {
    
    @Value("${kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;
    
    @Value("${kafka.consumer.group-id:noti-service}")
    private String groupId;
    
    @Value("${kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;
    
    /**
     * Kafka consumer configuration
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual acknowledgment
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.asyncsite.*");
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true); // Use type headers from producer
        
        // Type mappings are required when producer and consumer use different package structures
        // This is not a workaround but a proper configuration for microservices architecture
        config.put(JsonDeserializer.TYPE_MAPPINGS, 
            "com.asyncsite.userservice.auth.domain.event.PasskeyOtpRequestedEvent:" +
            "com.asyncsite.notiservice.adapter.in.event.dto.PasskeyOtpRequestedEvent");
        
        // Additional consumer properties for reliability
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5 minutes
        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000); // 30 seconds
        
        log.info("Kafka consumer configured with bootstrap servers: {}, group ID: {}", 
                bootstrapServers, groupId);
        
        return new DefaultKafkaConsumerFactory<>(config);
    }
    
    /**
     * Kafka listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // Enable manual acknowledgment
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        
        // Set concurrency for parallel processing
        factory.setConcurrency(3);
        
        // Error handling - retry 3 times with 1 second backoff
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler(
            new org.springframework.util.backoff.FixedBackOff(1000L, 3)
        ));
        
        log.info("Kafka listener container factory configured with manual acknowledgment and concurrency: 3");
        
        return factory;
    }
    
    /**
     * Kafka producer configuration (for future use - sending events)
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        // Idempotence for exactly-once semantics
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        log.info("Kafka producer configured with bootstrap servers: {}", bootstrapServers);
        
        return new DefaultKafkaProducerFactory<>(config);
    }
    
    /**
     * Kafka template for sending messages
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}