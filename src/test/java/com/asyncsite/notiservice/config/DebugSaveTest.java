package com.asyncsite.notiservice.config;

import com.asyncsite.notiservice.adapter.out.persistence.entity.NotificationTemplateEntity;
import com.asyncsite.notiservice.adapter.out.persistence.repository.NotificationTemplateRepository;
import com.asyncsite.notiservice.common.JsonUtil;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DebugSaveTest {

    @Autowired
    private NotificationTemplateRepository jpaRepository;

    @Test
    @Transactional
    void testVariablesSaveAndLoad() {
        // Given
        Map<String, String> variables = Map.of(
            "code", "123456",
            "expiryMinutes", "10"
        );
        
        String jsonVariables = JsonUtil.toJson(variables);
        System.out.println("JSON variables: " + jsonVariables);
        
        NotificationTemplate template = NotificationTemplate.builder()
            .templateId("test-var-template")
            .channelType(ChannelType.EMAIL)
            .eventType(EventType.ACTION)
            .titleTemplate("Test Title")
            .contentTemplate("Test Content")
            .variables(variables)
            .active(true)
            .version(null)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // When - Save using entity
        NotificationTemplateEntity entity = NotificationTemplateEntity.from(template);
        System.out.println("Entity variables before save: " + entity.getVariables());
        
        NotificationTemplateEntity saved = jpaRepository.save(entity);
        System.out.println("Entity variables after save: " + saved.getVariables());
        
        // Then - Load and check
        var found = jpaRepository.findById("test-var-template");
        assertThat(found).isPresent();
        
        System.out.println("Loaded entity variables: " + found.get().getVariables());
        
        NotificationTemplate domainModel = found.get().toDomain();
        System.out.println("Domain model variables: " + domainModel.getVariables());
        
        assertThat(domainModel.getVariables()).isNotNull();
        assertThat(domainModel.getVariables()).containsKeys("code", "expiryMinutes");
    }
}