package com.asyncsite.notiservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class YamlLoadTest {

    @Test
    void testYamlFileExists() throws Exception {
        ClassPathResource resource = new ClassPathResource("system-templates.yml");
        assertThat(resource.exists()).isTrue();
    }

    @Test
    void testYamlFileParsing() throws Exception {
        ClassPathResource resource = new ClassPathResource("system-templates.yml");
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        
        try (InputStream inputStream = resource.getInputStream()) {
            SystemTemplateInitializer.SystemTemplatesConfig config = 
                yamlMapper.readValue(inputStream, SystemTemplateInitializer.SystemTemplatesConfig.class);
            
            assertThat(config).isNotNull();
            assertThat(config.getVersion()).isNotNull();
            assertThat(config.getTemplates()).isNotEmpty();
            assertThat(config.getTemplates().size()).isEqualTo(8);

            // 템플릿 ID 확인
            var templateIds = config.getTemplates().stream()
                .map(SystemTemplateInitializer.TemplateConfig::getTemplateId)
                .toList();

            assertThat(templateIds).containsExactlyInAnyOrder(
                "passkey-otp",
                "password-reset",
                "welcome",
                "documento-analysis",
                "study-approved",
                "querydaily-question",
                "querydaily-answer-guide",
                "querydaily-application-confirmation"
            );
        }
    }
}