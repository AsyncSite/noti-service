package com.asyncsite.notiservice.adapter.out.persistence.specification;

import com.asyncsite.notiservice.adapter.out.persistence.entity.NotificationEntity;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.NotificationSearchCriteria;
import com.asyncsite.notiservice.domain.model.vo.NotificationStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationSpecifications {

    private NotificationSpecifications() {
        // Utility class
    }

    public static Specification<NotificationEntity> withCriteria(NotificationSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // userId 검색
            if (criteria.hasUserId()) {
                predicates.add(cb.equal(root.get("userId"), criteria.getUserId()));
            }

            // status 검색 (복수 선택 가능)
            if (criteria.hasStatuses()) {
                predicates.add(root.get("status").in(criteria.getStatuses()));
            }

            // templateId 검색
            if (criteria.hasTemplateId()) {
                predicates.add(cb.equal(root.get("templateId"), criteria.getTemplateId()));
            }

            // channelType 검색 (복수 선택 가능)
            if (criteria.hasChannelTypes()) {
                predicates.add(root.get("channelType").in(criteria.getChannelTypes()));
            }

            // 생성일 범위 검색
            if (criteria.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.getStartDate()));
            }
            if (criteria.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.getEndDate()));
            }

            // 예약 발송일 범위 검색
            if (criteria.getScheduledStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("scheduledAt"), criteria.getScheduledStartDate()));
            }
            if (criteria.getScheduledEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("scheduledAt"), criteria.getScheduledEndDate()));
            }

            // 키워드 검색 (title, content에서 검색)
            if (criteria.hasKeyword()) {
                String keyword = "%" + criteria.getKeyword().toLowerCase() + "%";
                Predicate titlePredicate = cb.like(cb.lower(root.get("title")), keyword);
                Predicate contentPredicate = cb.like(cb.lower(root.get("content")), keyword);
                predicates.add(cb.or(titlePredicate, contentPredicate));
            }

            // 실패 메시지 존재 여부
            if (criteria.getHasFailMessage() != null) {
                if (criteria.getHasFailMessage()) {
                    predicates.add(cb.isNotNull(root.get("failMessage")));
                } else {
                    predicates.add(cb.isNull(root.get("failMessage")));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // 개별 조건별 Specification 메서드
    public static Specification<NotificationEntity> hasUserId(String userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    public static Specification<NotificationEntity> hasStatus(NotificationStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<NotificationEntity> hasStatuses(List<NotificationStatus> statuses) {
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    public static Specification<NotificationEntity> hasChannelType(ChannelType channelType) {
        return (root, query, cb) -> cb.equal(root.get("channelType"), channelType);
    }

    public static Specification<NotificationEntity> hasChannelTypes(List<ChannelType> channelTypes) {
        return (root, query, cb) -> root.get("channelType").in(channelTypes);
    }

    public static Specification<NotificationEntity> hasTemplateId(String templateId) {
        return (root, query, cb) -> cb.equal(root.get("templateId"), templateId);
    }

    public static Specification<NotificationEntity> createdBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), end));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<NotificationEntity> scheduledBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("scheduledAt"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("scheduledAt"), end));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<NotificationEntity> containsKeyword(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            Predicate titlePredicate = cb.like(cb.lower(root.get("title")), pattern);
            Predicate contentPredicate = cb.like(cb.lower(root.get("content")), pattern);
            return cb.or(titlePredicate, contentPredicate);
        };
    }

    public static Specification<NotificationEntity> hasFailMessage(boolean hasFailMessage) {
        return (root, query, cb) -> hasFailMessage
            ? cb.isNotNull(root.get("failMessage"))
            : cb.isNull(root.get("failMessage"));
    }
}