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

    @Override
    public Optional<NotificationTemplate> findTemplateByChannelAndEventType(ChannelType channelType, EventType eventType) {
        return templateRepository.findByChannelTypeAndEventType(channelType, eventType).map(NotificationTemplateEntity::toDomain);
    }

    @Override
    public List<NotificationTemplate> findTemplatesByFilters(ChannelType channelType, Boolean isActive) {
        return templateRepository.findAllByChannelTypeAndActive(channelType, isActive)
                .stream().map(NotificationTemplateEntity::toDomain).toList();
    }
}
