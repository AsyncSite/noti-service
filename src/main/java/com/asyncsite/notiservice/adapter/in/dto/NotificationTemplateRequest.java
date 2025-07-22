package com.asyncsite.notiservice.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

public record NotificationTemplateRequest(
    @NotBlank(message = "이벤트 타입은 필수입니다.")
    String eventType,
    
    @NotBlank(message = "채널 타입은 필수입니다.")
    String channelType,
    
    @NotBlank(message = "언어는 필수입니다.")
    String language,
    
    @NotBlank(message = "제목 템플릿은 필수입니다.")
    String titleTemplate,
    
    @NotBlank(message = "내용 템플릿은 필수입니다.")
    String contentTemplate,
    
    List<String> variables,
    boolean isActive,
    Map<String, Object> previewVariables
) {
    // 기본값을 제공하는 생성자
    public NotificationTemplateRequest {
        // 기본값 설정
        if (language == null || language.isBlank()) {
            language = "ko";
        }
    }
}
