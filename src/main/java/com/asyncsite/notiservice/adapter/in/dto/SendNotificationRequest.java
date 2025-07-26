package com.asyncsite.notiservice.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record SendNotificationRequest(
    @NotNull(message = "사용자 ID는 필수입니다.")
    String userId,

    @NotBlank(message = "채널 타입은 필수입니다.")
    String channelType,
    @NotBlank(message = "채널 이벤트는 필수입니다.")
    String eventType,
    String recipientContact,

    Map<String, Object> metadata
) {}
