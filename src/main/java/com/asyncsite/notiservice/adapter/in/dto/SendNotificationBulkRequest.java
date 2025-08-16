package com.asyncsite.notiservice.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record SendNotificationBulkRequest(
        @NotNull(message = "사용자 ID는 필수입니다.")
    String userId,

        @NotBlank(message = "채널 타입은 필수입니다.")
    String channelType,

        @NotBlank(message = "채널 이벤트는 필수입니다.")
    String eventType,

        // 선택: 특정 템플릿을 강제로 지정하고 싶을 때만 사용
        String templateId,

        List<String> recipientContacts,

        // 템플릿 렌더링 변수
        Map<String, Object> variables
) {}
