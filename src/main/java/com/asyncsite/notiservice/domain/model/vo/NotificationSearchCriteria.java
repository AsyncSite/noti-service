package com.asyncsite.notiservice.domain.model.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class NotificationSearchCriteria {

    private final String userId;
    private final List<NotificationStatus> statuses;
    private final String templateId;
    private final List<ChannelType> channelTypes;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final String keyword;
    private final Boolean hasFailMessage;
    private final LocalDateTime scheduledStartDate;
    private final LocalDateTime scheduledEndDate;

    public boolean hasUserId() {
        return userId != null && !userId.isBlank();
    }

    public boolean hasStatuses() {
        return statuses != null && !statuses.isEmpty();
    }

    public boolean hasTemplateId() {
        return templateId != null && !templateId.isBlank();
    }

    public boolean hasChannelTypes() {
        return channelTypes != null && !channelTypes.isEmpty();
    }

    public boolean hasDateRange() {
        return startDate != null || endDate != null;
    }

    public boolean hasKeyword() {
        return keyword != null && !keyword.isBlank();
    }

    public boolean hasScheduledDateRange() {
        return scheduledStartDate != null || scheduledEndDate != null;
    }
}