package com.asyncsite.notiservice.adapter.out.persistence;

import com.asyncsite.notiservice.adapter.out.persistence.entity.NotificationTemplateEntity;
import com.asyncsite.notiservice.adapter.out.persistence.repository.NotificationTemplateRepository;
import com.asyncsite.notiservice.domain.model.NotificationChannel;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationTemplatePersistenceAdapter implements NotificationTemplateRepositoryPort {

    private final NotificationTemplateRepository templateRepository;

    @Override
    public NotificationTemplate saveTemplate(NotificationTemplate template) {
        NotificationTemplateEntity entity = NotificationTemplateEntity.from(template);
        if (entity.getTemplateId() == null) {
            entity = entity.toBuilder()
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        } else {
            entity = entity.toBuilder()
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        NotificationTemplateEntity savedEntity = templateRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<NotificationTemplate> findTemplateById(String templateId) {
        return templateRepository.findById(templateId)
                .map(NotificationTemplateEntity::toDomain);
    }

    @Override
    public Optional<NotificationTemplate> findTemplateByEventAndChannel(String eventType,
                                                                       NotificationChannel.ChannelType channelType,
                                                                       String language) {
        return templateRepository.findByEventTypeAndChannelTypeAndLanguageAndActive(eventType, channelType, language, true)
                .map(NotificationTemplateEntity::toDomain);
    }

    @Override
    public List<NotificationTemplate> findTemplatesByFilters(String eventType,
                                                            NotificationChannel.ChannelType channelType,
                                                            String language,
                                                            Boolean isActive,
                                                            int page,
                                                            int size) {
        // QueryDsl로 고도화 필요 현재 제대로 작동 x
        PageRequest pageRequest = PageRequest.of(page, size);
        return templateRepository.findAllByEventTypeAndChannelTypeAndLanguageAndActive(eventType, channelType, language, isActive, pageRequest)
                .getContent()
                .stream()
                .map(NotificationTemplateEntity::toDomain)
                .toList();
    }
}
