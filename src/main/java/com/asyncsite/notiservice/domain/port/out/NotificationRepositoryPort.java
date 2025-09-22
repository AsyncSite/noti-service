package com.asyncsite.notiservice.domain.port.out;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;

import java.util.List;
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
}
