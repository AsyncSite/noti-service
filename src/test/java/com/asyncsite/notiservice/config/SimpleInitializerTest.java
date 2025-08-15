package com.asyncsite.notiservice.config;

import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SimpleInitializerTest {

    @Autowired
    private SystemTemplateInitializer initializer;
    
    @Autowired
    private NotificationTemplateRepositoryPort repository;

    @Test
    void testInitializerRuns() throws Exception {
        // When
        initializer.run(null);
        
        // Then
        var templates = repository.findTemplates();
        assertThat(templates).isNotEmpty();
        assertThat(templates.size()).isGreaterThanOrEqualTo(4);
    }
}