package com.asyncsite.notiservice.domain.exception;

/**
 * 사용자가 특정 채널의 알림을 비활성화한 경우 발생하는 예외
 */
public class NotificationDisabledException extends RuntimeException {
    private final String userId;
    private final String channelType;

    public NotificationDisabledException(String userId, String channelType, String message) {
        super(message);
        this.userId = userId;
        this.channelType = channelType;
    }

    public NotificationDisabledException(String message) {
        super(message);
        this.userId = null;
        this.channelType = null;
    }

    public String getUserId() {
        return userId;
    }

    public String getChannelType() {
        return channelType;
    }
}