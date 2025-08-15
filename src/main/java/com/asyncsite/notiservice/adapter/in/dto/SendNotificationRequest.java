package com.asyncsite.notiservice.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record SendNotificationRequest(
    @NotNull(message = "사용자 ID는 필수입니다.")
    String userId,

    @NotBlank(message = "채널 타입은 필수입니다.")
    String channelType,

    // eventType은 더 이상 선택에 사용하지 않지만, 하위 호환을 위해 유지
    String eventType,

    @NotBlank(message = "템플릿 ID는 필수입니다.")
    String templateId,

    String recipientContact,

    // 템플릿 렌더링 변수
    Map<String, Object> variables
) {}
