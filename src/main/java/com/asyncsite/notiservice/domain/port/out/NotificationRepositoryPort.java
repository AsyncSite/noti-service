package com.asyncsite.notiservice.domain.port.out;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.NotificationSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface NotificationRepositoryPort {

    /**
     * 알림을 저장합니다.
     *
     * @param notification 저장할 알림
     * @return 저장된 알림
     */
    Notification saveNotification(Notification notification);

    /**
     * 알림 ID로 알림을 조회합니다.
     *
     * @param notificationId 알림 ID
     * @return 알림 정보 (없으면 empty)
     */
    Optional<Notification> findNotificationById(String notificationId);

    /**
     * 사용자의 알림 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 알림 목록
     */
    List<Notification> findNotificationsByUserId(String userId, ChannelType channelType, int page, int size);

    /**
     * CAS (Compare-And-Swap) 방식으로 알림 상태를 업데이트합니다.
     * 동시성 문제를 방지하기 위해 버전을 체크하며 업데이트합니다.
     *
     * @param notificationId 알림 ID
     * @param expectedVersion 예상 버전
     * @param notification 업데이트할 알림
     * @return 업데이트 성공 여부
     */
    boolean updateNotificationWithCAS(String notificationId, Long expectedVersion, Notification notification);

    /**
     * 처리할 예약된 알림을 조회하고 상태를 원자적으로 변경합니다.
     *
     * @param limit 조회할 알림 개수
     * @return 처리할 예약된 알림 목록
     */
    List<Notification> findAndLockScheduledNotifications(int limit);

    /**
     * 처리할 PENDING 알림을 조회합니다.
     *
     * @param limit 조회할 알림 개수
     * @return 처리할 PENDING 알림 목록
     */
    List<Notification> findPendingNotifications(int limit);

    /**
     * 백오피스용 전체 알림 목록을 조회합니다.
     * 모든 사용자의 알림을 페이징하여 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 알림 페이지
     */
    Page<Notification> findAllNotifications(Pageable pageable);

    /**
     * 백오피스용 알림 검색 기능입니다.
     * 다양한 조건으로 알림을 동적으로 검색합니다.
     *
     * @param criteria 검색 조건
     * @param pageable 페이징 정보
     * @return 검색된 알림 페이지
     */
    Page<Notification> searchNotifications(NotificationSearchCriteria criteria, Pageable pageable);

    /**
     * 알림 통계를 조회합니다.
     * 단일 쿼리로 모든 상태별 개수를 집계합니다.
     *
     * @return 상태별 알림 개수 맵 (total, sent, failed, pending, scheduled, retry, cancelled)
     */
    Map<String, Long> getNotificationStatistics();
}
