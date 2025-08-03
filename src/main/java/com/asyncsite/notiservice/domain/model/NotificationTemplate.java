package com.asyncsite.notiservice.domain.model;

import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Builder(toBuilder = true)
public class NotificationTemplate {
    private String templateId;
    private ChannelType channelType;
    private EventType eventType;
    private String titleTemplate;
    private String contentTemplate;
    private Map<String, String> variables;
    private boolean active;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    // === 정적 팩토리 메서드 ===

    /**
     * 새로운 알림 템플릿을 생성합니다.
     */
    public static NotificationTemplate create(
            ChannelType channelType,
            EventType eventType,
            String titleTemplate,
            String contentTemplate,
            Map<String, String> variables) {

        LocalDateTime now = LocalDateTime.now();
        return NotificationTemplate.builder()
                .channelType(channelType)
                .eventType(eventType)
                .titleTemplate(titleTemplate)
                .contentTemplate(contentTemplate)
                .variables(variables)
                .active(true)
                .version(0)
                .createdAt(now)
                .build();
    }

    // === 도메인 행위 메서드 ===

    /**
     * 템플릿을 업데이트합니다.
     */
    public NotificationTemplate updateTemplate(String titleTemplate, String contentTemplate, Map<String, String> variables) {
        return this.toBuilder()
                .titleTemplate(titleTemplate)
                .contentTemplate(contentTemplate)
                .variables(variables)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 템플릿을 활성화합니다.
     */
    public NotificationTemplate activate() {
        return this.toBuilder()
                .active(true)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 템플릿을 비활성화합니다.
     */
    public NotificationTemplate deactivate() {
        return this.toBuilder()
                .active(false)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // === 템플릿 렌더링 메서드 ===

    /**
     * 제목을 렌더링합니다.
     */
    public String renderTitle(Map<String, Object> data) {
        return renderTemplate(titleTemplate, data);
    }

    /**
     * 내용을 렌더링합니다.
     */
    public String renderContent(Map<String, Object> data) {
        return renderTemplate(contentTemplate, data);
    }

    /**
     * 템플릿을 렌더링합니다.
     */
    private String renderTemplate(String template, Map<String, Object> data) {
        if (template == null || template.trim().isEmpty()) {
            return "";
        }

        String result = template;
        Matcher matcher = VARIABLE_PATTERN.matcher(template);

        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            String placeholder = "{" + variableName + "}";
            Object value = data.get(variableName);
            String replacement = value != null ? value.toString() : getDefaultValue(variableName);
            result = result.replace(placeholder, replacement);
        }

        return result;
    }

    /**
     * 변수의 기본값을 가져옵니다.
     */
    private String getDefaultValue(String variableName) {
        if (variables != null && variables.containsKey(variableName)) {
            return variables.get(variableName);
        }
        return ""; // 기본값이 없으면 빈 문자열
    }

    /**
     * 템플릿이 사용 가능한지 확인합니다 (활성화되고 유효한 상태).
     */
    public boolean isUsable() {
        return isActive() && isValidTemplate();
    }

    /**
     * 템플릿이 유효한지 검증합니다.
     */
    public boolean isValidTemplate() {
        return isValidTemplateString(titleTemplate) && isValidTemplateString(contentTemplate);
    }

    /**
     * 템플릿 문자열이 유효한지 검증합니다.
     */
    private boolean isValidTemplateString(String template) {
        if (template == null || template.trim().isEmpty()) {
            return false;
        }

        // 중괄호가 올바르게 닫혀있는지 확인
        int openCount = 0;
        int closeCount = 0;
        for (int i = 0; i < template.length() - 1; i++) {
            if (template.charAt(i) == '{') {
                openCount++;
                i++; // 다음 문자 건너뛰기
            }
            if (template.charAt(i) == '}') {
                closeCount++;
                i++; // 다음 문자 건너뛰기
            }
        }

        return openCount == closeCount;
    }

    /**
     * 템플릿에 사용된 변수 목록을 추출합니다.
     */
    public java.util.Set<String> extractVariables() {
        java.util.Set<String> variableNames = new java.util.HashSet<>();

        extractVariablesFromTemplate(titleTemplate, variableNames);
        extractVariablesFromTemplate(contentTemplate, variableNames);

        return variableNames;
    }

    /**
     * 템플릿에서 변수를 추출합니다.
     */
    private void extractVariablesFromTemplate(String template, java.util.Set<String> variableNames) {
        if (template == null) return;

        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            variableNames.add(matcher.group(1).trim());
        }
    }
}
