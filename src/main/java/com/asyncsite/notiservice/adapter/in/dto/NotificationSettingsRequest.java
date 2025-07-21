package com.asyncsite.notiservice.adapter.in.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class NotificationSettingsRequest {
    
    private boolean studyUpdates = true;
    private boolean marketing = false;
    private boolean emailEnabled = true;
    private boolean discordEnabled = false;
    private boolean pushEnabled = false;
    private String timezone = "Asia/Seoul";
    private String language = "ko";
    private Map<String, Object> quietHours;
    private Map<String, Object> channelSettings;
    private boolean enabled;
    private List<String> channels;
} 