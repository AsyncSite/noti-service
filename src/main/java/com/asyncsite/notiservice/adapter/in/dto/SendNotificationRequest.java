package com.asyncsite.notiservice.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SendNotificationRequest {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private String userId;

    @NotBlank(message = "이벤트 타입은 필수입니다.")
    private String eventType;

    private Map<String, Object> metadata;
}
