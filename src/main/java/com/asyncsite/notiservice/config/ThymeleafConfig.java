package com.asyncsite.notiservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

@Slf4j
@Configuration
public class ThymeleafConfig {

    @Bean
    public SpringResourceTemplateResolver templateResolver(ApplicationContext applicationContext) {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setPrefix("classpath:/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false); // 개발 중에는 false, 운영에서는 true
        templateResolver.setCharacterEncoding("UTF-8");
        // Spring Boot 3.2+ nested JAR 문제 해결
        templateResolver.setCheckExistence(false); // nested JAR에서 존재 확인이 작동하지 않을 수 있음
        
        // 디버깅: 템플릿 경로 및 리소스 확인
        log.info("=== Thymeleaf Template Resolver 설정 ===");
        log.info("Prefix: {}", templateResolver.getPrefix());
        log.info("Suffix: {}", templateResolver.getSuffix());
        log.info("CheckExistence: {}", templateResolver.getCheckExistence());
        
        // email.html 파일 존재 여부 확인
        try {
            Resource emailResource = applicationContext.getResource("classpath:/templates/email.html");
            log.info("email.html 리소스 확인: exists={}, URL={}", 
                emailResource.exists(), 
                emailResource.exists() ? emailResource.getURL() : "N/A");
            
            // 클래스패스의 templates 디렉토리 내용 확인
            Resource templatesDir = applicationContext.getResource("classpath:/templates/");
            log.info("templates 디렉토리: exists={}, URL={}", 
                templatesDir.exists(), 
                templatesDir.exists() ? templatesDir.getURL() : "N/A");
        } catch (Exception e) {
            log.error("템플릿 리소스 확인 중 에러: ", e);
        }
        
        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine(SpringResourceTemplateResolver templateResolver) {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }
}