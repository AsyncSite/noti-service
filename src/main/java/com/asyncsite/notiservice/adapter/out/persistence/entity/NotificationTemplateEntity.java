package com.asyncsite.notiservice.adapter.out.persistence.entity;

import com.asyncsite.notiservice.domain.model.NotificationChannel;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "notification_templates")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateEntity {

    @Id
    @Column(name = "template_id")
    private String templateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false)
    private NotificationChannel.ChannelType channelType;

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "title_template", columnDefinition = "TEXT", nullable = false)
    private String titleTemplate;

    @Column(name = "content_template", columnDefinition = "TEXT", nullable = false)
    private String contentTemplate;

    @Column(name = "variables", columnDefinition = "JSON")
    private String variables;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "version")
    @Version
    private Integer version;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static NotificationTemplateEntity from(NotificationTemplate template) {
        return NotificationTemplateEntity.builder()
                .templateId(Strings.isEmpty(template.getTemplateId()) ?UUID.randomUUID().toString() : template.getTemplateId())
                .eventType(template.getEventType())
                .channelType(template.getChannelType())
                .language(template.getLanguage())
                .titleTemplate(template.getTitleTemplate())
                .contentTemplate(template.getContentTemplate())
                .variables(template.getVariables() != null ? template.getVariables().toString() : null)
                .active(template.isActive())
                .version(template.getVersion()) // 기본 버전
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    public NotificationTemplate toDomain() {
        return NotificationTemplate.builder()
                .templateId(this.templateId)
                .eventType(this.eventType)
                .channelType(this.channelType)
                .language(this.language)
                .titleTemplate(this.titleTemplate)
                .contentTemplate(this.contentTemplate)
                .variables(this.variables != null ? Map.of() : null) // TODO: JSON 파싱 구현
                .active(this.active)
                .version(this.version)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
