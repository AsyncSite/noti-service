package com.asyncsite.notiservice.config;

import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 시스템 필수 템플릿을 애플리케이션 시작 시 자동으로 등록/업데이트합니다.
 * system-templates.yml 파일에 정의된 템플릿을 데이터베이스와 동기화합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemTemplateInitializer implements ApplicationRunner {
    
    private final NotificationTemplateRepositoryPort templateRepository;
    private final PlatformTransactionManager transactionManager;
    private final Environment environment;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("==========================================");
        log.info("시스템 템플릿 초기화 시작");
        log.info("==========================================");
        
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        
        try {
            // 1. YAML 파일 로드
            SystemTemplatesConfig config = loadSystemTemplates();
            
            if (config == null) {
                log.error("시스템 템플릿 설정이 null입니다.");
                return;
            }
            
            log.debug("로드된 설정: {}", config);
            log.info("시스템 템플릿 버전: {}", config.getVersion());
            log.info("로드된 템플릿 개수: {}", config.getTemplates().size());
            
            int created = 0;
            int updated = 0;
            int skipped = 0;
            
            // 2. 각 템플릿 처리 (트랜잭션 내에서)
            for (TemplateConfig templateConfig : config.getTemplates()) {
                log.debug("템플릿 처리 중: {}", templateConfig.getTemplateId());
                ProcessResult result = transactionTemplate.execute(status -> 
                    processTemplate(templateConfig)
                );
                log.debug("템플릿 {} 처리 결과: {}", templateConfig.getTemplateId(), result);
                if (result != null) {
                    switch (result) {
                        case CREATED:
                            created++;
                            break;
                        case UPDATED:
                            updated++;
                            break;
                        case SKIPPED:
                            skipped++;
                            break;
                    }
                }
            }
            
            log.info("------------------------------------------");
            log.info("템플릿 초기화 결과:");
            log.info("  - 생성: {}개", created);
            log.info("  - 업데이트: {}개", updated);
            log.info("  - 스킵: {}개", skipped);
            log.info("==========================================");
            log.info("시스템 템플릿 초기화 완료");
            log.info("==========================================");
            
        } catch (Exception e) {
            log.error("시스템 템플릿 초기화 실패", e);
            // 시스템 템플릿은 중요하지만, 앱 시작을 막지는 않음
            // 운영 환경에서는 모니터링 알림 전송 고려
        }
    }
    
    private SystemTemplatesConfig loadSystemTemplates() throws Exception {
        ClassPathResource resource = new ClassPathResource("system-templates.yml");
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        
        try (InputStream inputStream = resource.getInputStream()) {
            return yamlMapper.readValue(inputStream, SystemTemplatesConfig.class);
        }
    }
    
    private ProcessResult processTemplate(TemplateConfig config) {
        try {
            String templateId = config.getTemplateId();
            log.debug("템플릿 처리 시작: {}", templateId);
            
            // 3. 기존 템플릿 확인
            Optional<NotificationTemplate> existing = 
                templateRepository.findTemplateById(templateId);
            
            if (existing.isPresent()) {
                // 4. 업데이트 필요 여부 확인
                NotificationTemplate existingTemplate = existing.get();
                if (shouldUpdate(existingTemplate, config)) {
                    updateTemplate(existingTemplate, config);
                    log.info("✅ 템플릿 업데이트 완료: {} [{}]", 
                        templateId, config.getChannelType());
                    return ProcessResult.UPDATED;
                } else {
                    log.debug("⏭️  템플릿 이미 최신: {}", templateId);
                    return ProcessResult.SKIPPED;
                }
            } else {
                // 5. 신규 템플릿 생성
                createTemplate(config);
                log.info("✅ 템플릿 생성 완료: {} [{}]", 
                    templateId, config.getChannelType());
                return ProcessResult.CREATED;
            }
            
        } catch (Exception e) {
            log.error("❌ 템플릿 처리 실패: {}", config.getTemplateId(), e);
            return ProcessResult.ERROR;
        }
    }
    
    private boolean shouldUpdate(NotificationTemplate existing, TemplateConfig config) {
        // 환경 변수 치환 후 비교
        Map<String, String> processedVariables = replaceEnvironmentVariables(config.getVariables());

        // 템플릿 내용이나 활성화 상태가 변경되었는지 확인
        boolean titleChanged = !nullSafeEquals(existing.getTitleTemplate(), config.getTitleTemplate());
        boolean contentChanged = !nullSafeEquals(existing.getContentTemplate(), config.getContentTemplate());
        boolean activeChanged = existing.isActive() != config.isActive();
        boolean variablesChanged = !nullSafeMapEquals(existing.getVariables(), processedVariables);
        boolean mailConfigChanged = !nullSafeEquals(existing.getMailConfigName(), config.getMailConfigName());

        // 문자셋 손상 감지 - 템플릿에 '?' 문자가 포함되어 있으면 강제 업데이트
        boolean charsetCorrupted = isCharsetCorrupted(existing);

        if (charsetCorrupted) {
            log.warn("⚠️ 템플릿 문자셋 손상 감지 - ID: {}, 강제 업데이트 수행", existing.getTemplateId());
            return true;
        }

        if (titleChanged || contentChanged || activeChanged || variablesChanged || mailConfigChanged) {
            log.debug("템플릿 변경 감지 - ID: {}, 제목변경: {}, 내용변경: {}, 활성화변경: {}, 변수변경: {}, 메일설정변경: {}",
                existing.getTemplateId(), titleChanged, contentChanged, activeChanged, variablesChanged, mailConfigChanged);
            return true;
        }
        
        return false;
    }
    
    private boolean isCharsetCorrupted(NotificationTemplate template) {
        // 한글이 깨져서 '?'로 표시되는 경우를 감지
        // 정상적인 템플릿에서는 연속된 '?' 문자가 나타나지 않아야 함
        String title = template.getTitleTemplate();
        String content = template.getContentTemplate();
        
        // 연속된 물음표 패턴 감지 (한글이 깨진 경우 보통 "???"처럼 나타남)
        String corruptedPattern = "\\?{2,}";
        
        boolean titleCorrupted = title != null && title.matches(".*" + corruptedPattern + ".*");
        boolean contentCorrupted = content != null && content.matches(".*" + corruptedPattern + ".*");
        
        // 단일 '?'가 비정상적으로 많은 경우도 감지
        boolean titleSuspicious = title != null && countCharOccurrences(title, '?') > 3;
        boolean contentSuspicious = content != null && countCharOccurrences(content, '?') > 5;
        
        return titleCorrupted || contentCorrupted || titleSuspicious || contentSuspicious;
    }
    
    private int countCharOccurrences(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }
    
    private boolean nullSafeEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
    
    private boolean nullSafeMapEquals(Map<String, String> a, Map<String, String> b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
    
    private void updateTemplate(NotificationTemplate existing, TemplateConfig config) {
        // 환경 변수 치환
        Map<String, String> processedVariables = replaceEnvironmentVariables(config.getVariables());

        NotificationTemplate updated = existing
            .updateTemplate(
                config.getTitleTemplate(),
                config.getContentTemplate(),
                processedVariables
            );

        // mailConfigName 업데이트
        if (config.getMailConfigName() != null &&
            !config.getMailConfigName().equals(existing.getMailConfigName())) {
            updated = updated.toBuilder()
                .mailConfigName(config.getMailConfigName())
                .updatedAt(LocalDateTime.now())
                .build();
            log.debug("템플릿 메일 설정 변경: {} -> {}", existing.getMailConfigName(), config.getMailConfigName());
        }

        // 활성화 상태 변경
        if (config.isActive() && !existing.isActive()) {
            updated = updated.activate();
            log.debug("템플릿 활성화: {}", config.getTemplateId());
        } else if (!config.isActive() && existing.isActive()) {
            updated = updated.deactivate();
            log.debug("템플릿 비활성화: {}", config.getTemplateId());
        }

        templateRepository.saveTemplate(updated);
    }
    
    private void createTemplate(TemplateConfig config) {
        LocalDateTime now = LocalDateTime.now();

        // 환경 변수 치환
        Map<String, String> processedVariables = replaceEnvironmentVariables(config.getVariables());

        log.debug("Creating template {} with variables: {}", config.getTemplateId(), processedVariables);

        NotificationTemplate template = NotificationTemplate.builder()
            .templateId(config.getTemplateId())
            .channelType(ChannelType.valueOf(config.getChannelType().toUpperCase()))
            .eventType(EventType.valueOf(config.getEventType().toUpperCase()))
            .titleTemplate(config.getTitleTemplate())
            .contentTemplate(config.getContentTemplate())
            .variables(processedVariables)
            .isDefault(false)
            .priority(0)
            .active(config.isActive())
            .version(null)  // JPA @Version 필드는 null로 시작
            .createdAt(now)
            .updatedAt(now)
            .mailConfigName(config.getMailConfigName())
            .build();
        
        templateRepository.saveTemplate(template);
        
        log.debug("새 템플릿 생성 - ID: {}, 채널: {}, 이벤트: {}", 
            template.getTemplateId(), 
            template.getChannelType(), 
            template.getEventType());
    }
    
    // === 설정 클래스들 ===
    
    @Data
    public static class SystemTemplatesConfig {
        private String version;
        private List<TemplateConfig> templates;
        
        public List<TemplateConfig> getTemplates() {
            return templates != null ? templates : List.of();
        }
    }
    
    @Data
    public static class TemplateConfig {
        private String templateId;
        private String channelType;
        private String eventType;
        private String titleTemplate;
        private String contentTemplate;
        private Map<String, String> variables;
        private boolean active = true;
        private String mailConfigName;
    }
    
    private enum ProcessResult {
        CREATED,
        UPDATED,
        SKIPPED,
        ERROR
    }

    /**
     * 템플릿 변수에서 ${...} 패턴의 환경 변수 참조를 실제 값으로 치환합니다.
     *
     * @param variables 원본 템플릿 변수 맵
     * @return 환경 변수가 치환된 변수 맵
     */
    private Map<String, String> replaceEnvironmentVariables(Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) {
            return variables;
        }

        Map<String, String> processedVariables = new HashMap<>();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // ${...} 패턴을 찾아서 환경 변수로 치환
            if (value != null && value.startsWith("${") && value.endsWith("}")) {
                String propertyName = value.substring(2, value.length() - 1);

                // QueryDaily 관련 특별 처리
                if ("application.querydaily.base-url".equals(propertyName)) {
                    String resolvedValue = environment.getProperty(propertyName, "https://querydaily.asyncsite.com");
                    processedVariables.put(key, resolvedValue);
                    log.debug("환경 변수 치환: {} = {} -> {}", key, value, resolvedValue);
                } else {
                    // 다른 환경 변수 처리
                    String resolvedValue = environment.getProperty(propertyName, value);
                    processedVariables.put(key, resolvedValue);
                }
            } else {
                // 환경 변수 참조가 아닌 경우 그대로 사용
                processedVariables.put(key, value);
            }
        }

        return processedVariables;
    }
}