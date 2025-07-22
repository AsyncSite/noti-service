package com.asyncsite.notiservice.adapter.out.persistence.repository;

import com.asyncsite.notiservice.adapter.out.persistence.entity.NotificationTemplateEntity;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface  NotificationTemplateRepository extends JpaRepository<NotificationTemplateEntity, String> {

    List<NotificationTemplateEntity> findAllByChannelType(ChannelType channelType);
}
