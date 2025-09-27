package com.asyncsite.notiservice.adapter.out.persistence.repository;

import com.asyncsite.notiservice.adapter.out.persistence.entity.NotificationEntity;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String>, JpaSpecificationExecutor<NotificationEntity> {

    Page<NotificationEntity> findByUserIdAndChannelTypeOrderByCreatedAtDesc(String userId, ChannelType channelType, Pageable pageable);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.status = :status, n.updatedAt = :updatedAt, n.version = n.version + 1 " +
           "WHERE n.notificationId = :notificationId AND n.version = :expectedVersion")
    int updateStatusWithCAS(@Param("notificationId") String notificationId,
                           @Param("expectedVersion") Long expectedVersion,
                           @Param("status") NotificationStatus status,
                           @Param("updatedAt") LocalDateTime updatedAt);

    @Query("SELECT n FROM NotificationEntity n WHERE n.status = :scheduledStatus " +
           "AND n.scheduledAt IS NOT NULL AND n.scheduledAt <= :now " +
           "ORDER BY n.scheduledAt ASC")
    List<NotificationEntity> findScheduledNotificationsToProcess(@Param("scheduledStatus") NotificationStatus scheduledStatus,
                                                                @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT n FROM NotificationEntity n WHERE n.status = :pendingStatus " +
           "ORDER BY n.createdAt ASC")
    List<NotificationEntity> findPendingNotifications(@Param("pendingStatus") NotificationStatus pendingStatus, Pageable pageable);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.status = :pendingStatus, n.updatedAt = :now, n.version = n.version + 1 " +
           "WHERE n.notificationId IN :notificationIds AND n.status = :scheduledStatus")
    int markScheduledAsPending(@Param("notificationIds") List<String> notificationIds,
                             @Param("pendingStatus") NotificationStatus pendingStatus,
                             @Param("scheduledStatus") NotificationStatus scheduledStatus,
                             @Param("now") LocalDateTime now);

    @Query(value = """
        SELECT
            CAST(COUNT(*) AS SIGNED) as total,
            CAST(SUM(CASE WHEN status = 'SENT' THEN 1 ELSE 0 END) AS SIGNED) as sent,
            CAST(SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) AS SIGNED) as failed,
            CAST(SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) AS SIGNED) as pending,
            CAST(SUM(CASE WHEN status = 'SCHEDULED' THEN 1 ELSE 0 END) AS SIGNED) as scheduled,
            CAST(SUM(CASE WHEN status = 'RETRY' THEN 1 ELSE 0 END) AS SIGNED) as retry,
            CAST(SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) AS SIGNED) as cancelled
        FROM notifications
        """, nativeQuery = true)
    Map<String, Long> getNotificationStatistics();
}
