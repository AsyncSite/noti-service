// This file has been removed as Kafka configuration is now provided by Common module
// Common module provides:
// - Consumer configuration with JsonNode as default type (no type headers)
// - Manual acknowledgment mode
// - Error handling with retry
// - Correlation ID handling
// 
// Services using Common module automatically get Kafka configuration via:
// com.asyncsite.coreplatform.common.config.KafkaConsumerConfig
//
// To customize settings, use application.yml:
// spring:
//   kafka:
//     bootstrap-servers: ...
//     consumer:
//       group-id: ...
//       auto-offset-reset: ...