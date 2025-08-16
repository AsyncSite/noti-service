package com.asyncsite.notiservice.adapter.in.web.dto;

import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;

import java.util.Map;

public record CreateNotificationTemplateRequest (
        ChannelType channelType,
        EventType eventType,
        String titleTemplate,
        String contentTemplate,
        Map<String, String> variables
){
}
