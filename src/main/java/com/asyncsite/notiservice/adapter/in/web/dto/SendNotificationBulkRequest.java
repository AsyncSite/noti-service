package com.asyncsite.notiservice.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.util.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SendNotificationBulkRequest(
        @NotNull(message = "사용자 ID는 필수입니다.")
    String userId,

        @NotBlank(message = "채널 타입은 필수입니다.")
    String channelType,

        @NotBlank(message = "채널 이벤트는 필수입니다.")
    String eventType,

        // 선택: 특정 템플릿을 강제로 지정하고 싶을 때만 사용
        String templateId,

        List<String> recipientContacts,

        // 템플릿 렌더링 변수
        Map<String, Object> variables
) {
    public Map<String, Object> getMetaData() {
        Map<String, Object> meta = new HashMap<>();
        if (!Strings.isEmpty(this.templateId) && !this.templateId.isBlank()) {
            meta.put("templateId", this.templateId);
        }
        meta.put("variables", Objects.isNull(this.variables) ? java.util.Map.of() : this.variables);
        return meta;
    }
}
