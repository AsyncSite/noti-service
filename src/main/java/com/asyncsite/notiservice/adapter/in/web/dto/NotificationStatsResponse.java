package com.asyncsite.notiservice.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 통계 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatsResponse {
    private Long total;
    private Long sent;
    private Long failed;
    private Long pending;
    private Long scheduled;
    private Long retry;
    private Long cancelled;
}