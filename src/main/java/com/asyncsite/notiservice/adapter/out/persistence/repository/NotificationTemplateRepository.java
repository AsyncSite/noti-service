package com.asyncsite.notiservice.adapter.out.persistence.repository;

import com.asyncsite.notiservice.adapter.out.persistence.entity.NotificationTemplateEntity;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface  NotificationTemplateRepository extends JpaRepository<NotificationTemplateEntity, String> {

    Optional<NotificationTemplateEntity> findByChannelTypeAndEventType(ChannelType channelType, EventType eventType);
    List<NotificationTemplateEntity> findAllByChannelType(ChannelType channelType);
    List<NotificationTemplateEntity> findAllByChannelTypeAndActive(ChannelType channelType, boolean active);
}
