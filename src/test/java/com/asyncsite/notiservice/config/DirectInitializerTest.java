package com.asyncsite.notiservice.config;

import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
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
class DirectInitializerTest {

    @Autowired
    private NotificationTemplateRepositoryPort repository;

    @Test
    @Transactional
    void testDirectSave() {
        // Given
        NotificationTemplate template = NotificationTemplate.builder()
            .templateId("test-template")
            .channelType(ChannelType.EMAIL)
            .eventType(EventType.ACTION)
            .titleTemplate("Test Title")
            .contentTemplate("Test Content")
            .variables(Map.of("var1", "value1"))
            .active(true)
            .version(null)  // JPA @Version 필드는 null로 시작
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // When
        repository.saveTemplate(template);

        // Then
        var saved = repository.findTemplateById("test-template");
        assertThat(saved).isPresent();
        assertThat(saved.get().getTitleTemplate()).isEqualTo("Test Title");
    }
}