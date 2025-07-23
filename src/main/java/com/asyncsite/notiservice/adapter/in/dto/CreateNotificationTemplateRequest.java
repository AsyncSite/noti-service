package com.asyncsite.notiservice.adapter.in.dto;

import com.asyncsite.notiservice.domain.model.vo.ChannelType;

import java.util.Map;

public record CreateNotificationTemplateRequest (
        ChannelType channelType,
        String titleTemplate,
        String contentTemplate,
        Map<String, String> variables
){
}
