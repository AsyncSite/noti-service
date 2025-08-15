package com.asyncsite.notiservice.config;

import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DebugInitializerTest {

    @Autowired
    private SystemTemplateInitializer initializer;
    
    @Autowired
    private NotificationTemplateRepositoryPort repository;

    @Test
    void debugTemplateContents() throws Exception {
        // When
        initializer.run(null);
        
        // Then
        var passkeyOtp = repository.findTemplateById("passkey-otp");
        
        if (passkeyOtp.isPresent()) {
            var template = passkeyOtp.get();
            System.out.println("=== PASSKEY-OTP TEMPLATE ===");
            System.out.println("Title: " + template.getTitleTemplate());
            System.out.println("Channel: " + template.getChannelType());
            System.out.println("Event: " + template.getEventType());
            System.out.println("Variables: " + template.getVariables());
            System.out.println("Variables class: " + (template.getVariables() != null ? template.getVariables().getClass() : "null"));
            System.out.println("Active: " + template.isActive());
            System.out.println("Version: " + template.getVersion());
        } else {
            System.out.println("Template not found!");
        }
        
        assertThat(passkeyOtp).isPresent();
    }
}