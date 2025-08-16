package com.asyncsite.notiservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

@Slf4j
@Configuration
public class ThymeleafConfig {

    @Bean
    public ClassLoaderTemplateResolver templateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        // ClassLoaderTemplateResolver는 classpath: 없이 상대 경로 사용
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false); // 개발 중에는 false, 운영에서는 true
        templateResolver.setCharacterEncoding("UTF-8");
        
        // 디버깅: 템플릿 경로 설정 확인
        log.info("=== Thymeleaf ClassLoaderTemplateResolver 설정 ===");
        log.info("Prefix: {}", templateResolver.getPrefix());
        log.info("Suffix: {}", templateResolver.getSuffix());
        log.info("CharacterEncoding: {}", templateResolver.getCharacterEncoding());
        log.info("Cacheable: {}", templateResolver.isCacheable());
        
        // ClassLoader를 통한 리소스 확인
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            var emailResource = classLoader.getResource("templates/email.html");
            log.info("email.html 리소스 확인: {}", emailResource != null ? emailResource.toString() : "NOT FOUND");
        } catch (Exception e) {
            log.error("템플릿 리소스 확인 중 에러: ", e);
        }
        
        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine(ClassLoaderTemplateResolver templateResolver) {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }
}