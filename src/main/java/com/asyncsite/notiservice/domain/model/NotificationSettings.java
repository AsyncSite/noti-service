package com.asyncsite.notiservice.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder(toBuilder = true)
public class NotificationSettings {
    private String userId;
    private boolean studyUpdates;
    private boolean marketing;
    private boolean emailEnabled;
    private boolean discordEnabled;
    private boolean pushEnabled;
    private String timezone;
    private String language;
    private Map<String, Object> quietHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isChannelEnabled(NotificationChannel.ChannelType channelType) {
        return switch (channelType) {
            case EMAIL -> emailEnabled;
            case DISCORD -> discordEnabled;
            case PUSH -> pushEnabled;
        };
    }

    public boolean isEventTypeEnabled(String eventType) {
        if (eventType.startsWith("STUDY_")) {
            return studyUpdates;
        }
        if (eventType.startsWith("MARKETING_")) {
            return marketing;
        }
        return true; // 기본적으로 활성화
    }

    public boolean isInQuietHours() {
        if (quietHours == null || quietHours.isEmpty()) return false;
        try {
            boolean enabled = Boolean.TRUE.equals(quietHours.get("enabled"));
            if (!enabled) return false;
            String startTime = (String) quietHours.get("startTime");
            String endTime = (String) quietHours.get("endTime");
            boolean weekendsOnly = Boolean.TRUE.equals(quietHours.get("weekendsOnly"));
            java.time.LocalTime now = java.time.LocalTime.now(java.time.ZoneId.of(timezone != null ? timezone : "Asia/Seoul"));
            java.time.DayOfWeek day = java.time.LocalDate.now(java.time.ZoneId.of(timezone != null ? timezone : "Asia/Seoul")).getDayOfWeek();
            if (weekendsOnly && (day != java.time.DayOfWeek.SATURDAY && day != java.time.DayOfWeek.SUNDAY)) {
                return false;
            }
            java.time.LocalTime start = java.time.LocalTime.parse(startTime);
            java.time.LocalTime end = java.time.LocalTime.parse(endTime);
            if (start.isBefore(end)) {
                return now.isAfter(start) && now.isBefore(end);
            } else {
                // 밤 10시~아침 8시 등, 자정 넘는 구간
                return now.isAfter(start) || now.isBefore(end);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public NotificationSettings withChannelEnabled(NotificationChannel.ChannelType channelType, boolean enabled) {
        return NotificationSettings.builder()
                .userId(this.userId)
                .studyUpdates(this.studyUpdates)
                .marketing(this.marketing)
                .emailEnabled(channelType == NotificationChannel.ChannelType.EMAIL ? enabled : this.emailEnabled)
                .discordEnabled(channelType == NotificationChannel.ChannelType.DISCORD ? enabled : this.discordEnabled)
                .pushEnabled(channelType == NotificationChannel.ChannelType.PUSH ? enabled : this.pushEnabled)
                .timezone(this.timezone)
                .language(this.language)
                .quietHours(this.quietHours)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
