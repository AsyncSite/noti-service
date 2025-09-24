package com.asyncsite.notiservice.adapter.in.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryApplicationCreatedEvent {
    private Long queryId;
    private String email;
    private String name;
    private String resumeFileName;
    private String assetId;
    private LocalDateTime createdAt;
    private String eventType;
    private String eventId;
    private Long timestamp;
}