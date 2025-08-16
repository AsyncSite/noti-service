package com.asyncsite.notiservice.adapter.in.web.dto;

import java.util.Map;

public record UpdateNotificationTemplateRequest(
        String titleTemplate,
        String contentTemplate,
        Map<String, String> variables
) {
}
