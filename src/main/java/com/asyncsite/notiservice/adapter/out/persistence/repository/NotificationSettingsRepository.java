package com.asyncsite.notiservice.adapter.out.persistence.repository;

import com.asyncsite.notiservice.adapter.out.persistence.entity.NotificationSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettingsEntity, String> {
}
