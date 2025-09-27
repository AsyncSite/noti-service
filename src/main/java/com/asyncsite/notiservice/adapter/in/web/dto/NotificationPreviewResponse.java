package com.asyncsite.notiservice.adapter.in.web.dto;

/**
 * 알림 미리보기 응답 DTO
 * 렌더링된 HTML 콘텐츠를 반환
 */
public record NotificationPreviewResponse(
        String htmlContent
) {}