package com.asyncsite.notiservice.adapter.in.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationEvent {

    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("occurredAt")
    private Instant occurredAt;

    @JsonProperty("eventVersion")
    private String eventVersion;

    // --------- 여기부터 Notification 정보 -----------

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("channelType")
    private String channelType;

    @JsonProperty("data")
    private Map<String, Object> data;

    @JsonProperty("from")
    private String userId;

    @JsonProperty("to")
    private List<String> recipientContacts;
}