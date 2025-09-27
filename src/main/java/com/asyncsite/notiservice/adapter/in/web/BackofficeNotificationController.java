package com.asyncsite.notiservice.adapter.in.web;

import com.asyncsite.coreplatform.common.dto.ApiResponse;
import com.asyncsite.notiservice.adapter.in.web.dto.BackofficeNotificationResponse;
import com.asyncsite.notiservice.adapter.in.web.dto.NotificationStatsResponse;
import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.NotificationSearchCriteria;
import com.asyncsite.notiservice.domain.model.vo.NotificationStatus;
import com.asyncsite.notiservice.domain.port.in.NotificationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 백오피스 알림 관리 API 컨트롤러
 * 관리자 전용 알림 조회 및 관리 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/noti/admin")
@RequiredArgsConstructor
public class BackofficeNotificationController {

    private final NotificationUseCase notificationUseCase;

    /**
     * 전체 알림 목록 조회 (백오피스용)
     * 모든 사용자의 알림을 최신순으로 조회합니다.
     *
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 20, 최대: 100)
     * @return 알림 목록 페이지 응답
     */
    @GetMapping("/all")
    public ApiResponse<Page<BackofficeNotificationResponse>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // 페이지 크기 제한 (최대 100개)
        if (size > 100) {
            size = 100;
            log.warn("페이지 크기가 100을 초과하여 100으로 조정됨");
        }

        // 최신순 정렬 (created_at DESC)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        log.info("백오피스 전체 알림 조회 요청: page={}, size={}", page, size);

        // 서비스 호출
        Page<Notification> notificationPage = notificationUseCase.getAllNotifications(pageable);

        // DTO 변환 및 응답
        Page<BackofficeNotificationResponse> pageResponse = notificationPage.map(BackofficeNotificationResponse::from);

        log.info("백오피스 전체 알림 조회 완료: totalElements={}, totalPages={}, currentPage={}",
                pageResponse.getTotalElements(), pageResponse.getTotalPages(), pageResponse.getNumber());

        return ApiResponse.success(pageResponse);
    }

    /**
     * 백오피스용 알림 검색 API
     * 다양한 조건으로 알림을 검색할 수 있습니다.
     *
     * @param userId 사용자 ID (선택)
     * @param statuses 알림 상태 목록 (선택, 콤마로 구분)
     * @param templateId 템플릿 ID (선택)
     * @param channelTypes 채널 타입 목록 (선택, 콤마로 구분)
     * @param startDate 생성일 시작 범위 (선택)
     * @param endDate 생성일 종료 범위 (선택)
     * @param scheduledStartDate 예약일 시작 범위 (선택)
     * @param scheduledEndDate 예약일 종료 범위 (선택)
     * @param keyword 검색 키워드 - 제목/내용에서 검색 (선택)
     * @param hasFailMessage 실패 메시지 존재 여부 (선택)
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 20, 최대: 100)
     * @return 검색된 알림 목록 페이지 응답
     */
    @GetMapping("/search")
    public ApiResponse<Page<BackofficeNotificationResponse>> searchNotifications(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String statuses,
            @RequestParam(required = false) String templateId,
            @RequestParam(required = false) String channelTypes,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) LocalDateTime scheduledStartDate,
            @RequestParam(required = false) LocalDateTime scheduledEndDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean hasFailMessage,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // 페이지 크기 제한 (최대 100개)
        if (size > 100) {
            size = 100;
            log.warn("페이지 크기가 100을 초과하여 100으로 조정됨");
        }

        // 상태 목록 파싱
        List<NotificationStatus> statusList = null;
        if (statuses != null && !statuses.isBlank()) {
            statusList = Arrays.stream(statuses.split(","))
                    .map(String::trim)
                    .map(NotificationStatus::valueOf)
                    .collect(Collectors.toList());
        }

        // 채널 타입 목록 파싱
        List<ChannelType> channelTypeList = null;
        if (channelTypes != null && !channelTypes.isBlank()) {
            channelTypeList = Arrays.stream(channelTypes.split(","))
                    .map(String::trim)
                    .map(ChannelType::valueOf)
                    .collect(Collectors.toList());
        }

        // 검색 조건 생성
        NotificationSearchCriteria criteria = NotificationSearchCriteria.builder()
                .userId(userId)
                .statuses(statusList)
                .templateId(templateId)
                .channelTypes(channelTypeList)
                .startDate(startDate)
                .endDate(endDate)
                .scheduledStartDate(scheduledStartDate)
                .scheduledEndDate(scheduledEndDate)
                .keyword(keyword)
                .hasFailMessage(hasFailMessage)
                .build();

        // 페이징 정보 생성 (최신순 정렬)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        log.info("백오피스 알림 검색 요청: criteria={}, page={}, size={}", criteria, page, size);

        // 서비스 호출
        Page<Notification> notificationPage = notificationUseCase.searchNotifications(criteria, pageable);

        // DTO 변환 및 응답
        Page<BackofficeNotificationResponse> pageResponse = notificationPage.map(BackofficeNotificationResponse::from);

        log.info("백오피스 알림 검색 완료: totalElements={}, totalPages={}, currentPage={}",
                pageResponse.getTotalElements(), pageResponse.getTotalPages(), pageResponse.getNumber());

        return ApiResponse.success(pageResponse);
    }

    /**
     * 알림 통계 조회 (백오피스용)
     * 전체 알림의 상태별 통계를 조회합니다.
     *
     * @return 알림 통계 응답
     */
    @GetMapping("/stats")
    public ApiResponse<NotificationStatsResponse> getNotificationStats() {
        log.info("백오피스 알림 통계 조회 요청");

        NotificationStatsResponse stats = notificationUseCase.getNotificationStats();

        log.info("백오피스 알림 통계 조회 완료: total={}, sent={}, failed={}, pending={}, scheduled={}",
                stats.getTotal(), stats.getSent(), stats.getFailed(), stats.getPending(), stats.getScheduled());

        return ApiResponse.success(stats);
    }
}