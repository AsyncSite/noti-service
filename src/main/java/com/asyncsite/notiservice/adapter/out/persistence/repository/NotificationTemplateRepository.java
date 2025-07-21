package com.asyncsite.notiservice.adapter.out.persistence.repository;

import com.asyncsite.notiservice.adapter.out.persistence.entity.NotificationTemplateEntity;
import com.asyncsite.notiservice.domain.model.NotificationChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplateEntity, String> {

    Optional<NotificationTemplateEntity> findByEventTypeAndChannelTypeAndLanguageAndActive(String eventType, NotificationChannel.ChannelType channelType, String language, boolean isActive);
    Page<NotificationTemplateEntity> findAllByEventTypeAndChannelTypeAndLanguageAndActive(
            String eventType,
            NotificationChannel.ChannelType channelType,
            String language,
            Boolean isActive,
            Pageable pageable);
}
