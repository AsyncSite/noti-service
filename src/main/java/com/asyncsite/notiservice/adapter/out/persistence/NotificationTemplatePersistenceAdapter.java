package com.asyncsite.notiservice.adapter.out.persistence;

import com.asyncsite.notiservice.adapter.out.persistence.entity.NotificationTemplateEntity;
import com.asyncsite.notiservice.adapter.out.persistence.repository.NotificationTemplateRepository;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationTemplatePersistenceAdapter implements NotificationTemplateRepositoryPort {

    private final NotificationTemplateRepository templateRepository;

    @Override
    public NotificationTemplate saveTemplate(NotificationTemplate template) {
        NotificationTemplateEntity entity = NotificationTemplateEntity.from(template);
        NotificationTemplateEntity savedEntity = templateRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<NotificationTemplate> findTemplateById(String templateId) {
        return templateRepository.findById(templateId)
                .map(NotificationTemplateEntity::toDomain);
    }

    @Override
    public List<NotificationTemplate> findTemplateByChannel(ChannelType channelType) {
        return templateRepository.findAllByChannelType(channelType).stream().map(NotificationTemplateEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<NotificationTemplate> findTemplates() {
        return templateRepository.findAll().stream().map(NotificationTemplateEntity::toDomain).collect(Collectors.toList());
    }

    // (channel,event) 단일 조회는 제거되었습니다. templateId로만 선택합니다.

    @Override
    public List<NotificationTemplate> findTemplatesByFilters(ChannelType channelType, Boolean isActive) {
        return templateRepository.findAllByChannelTypeAndActive(channelType, isActive)
                .stream().map(NotificationTemplateEntity::toDomain).toList();
    }

    @Override
    public Optional<NotificationTemplate> findDefaultTemplate(ChannelType channelType, EventType eventType) {
        return templateRepository
                .findFirstByChannelTypeAndEventTypeAndActiveAndIsDefault(channelType, eventType, true, true)
                .map(NotificationTemplateEntity::toDomain);
    }

    @Override
    public List<NotificationTemplate> findActiveTemplatesByChannelAndEvent(ChannelType channelType, EventType eventType) {
        return templateRepository
                .findAllByChannelTypeAndEventTypeAndActiveOrderByPriorityDescUpdatedAtDesc(channelType, eventType, true)
                .stream().map(NotificationTemplateEntity::toDomain).toList();
    }
}
