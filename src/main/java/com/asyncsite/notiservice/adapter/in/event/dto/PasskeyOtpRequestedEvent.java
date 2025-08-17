package com.asyncsite.notiservice.adapter.in.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Event DTO for passkey OTP request events received from user-service.
 * This class represents the deserialized form of the PasskeyOtpRequestedEvent from Kafka.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PasskeyOtpRequestedEvent {
    
    @JsonProperty("eventId")
    private String eventId;
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("occurredAt")
    private Instant occurredAt;
    
    @JsonProperty("eventVersion")
    private String eventVersion;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("otpCode")
    private String otpCode;
    
    @JsonProperty("expiryMinutes")
    private Long expiryMinutes;
    
    @JsonProperty("clientIp")
    private String clientIp;
    
    @JsonProperty("requestedVia")
    private String requestedVia;
}