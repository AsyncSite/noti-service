package com.asyncsite.notiservice.config;

import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("시스템 템플릿 초기화 통합 테스트")
class SystemTemplateInitializerIntegrationTest {

    @Autowired
    private SystemTemplateInitializer systemTemplateInitializer;
    
    @Autowired
    private NotificationTemplateRepositoryPort templateRepository;

    @BeforeEach
    void setUp() throws Exception {
        // ApplicationRunner는 테스트에서 자동 실행되지 않으므로 수동 실행
        systemTemplateInitializer.run(null);
    }

    @Test
    @DisplayName("시스템 템플릿 4개가 모두 로드된다")
    void allSystemTemplatesAreLoaded() {
        // When
        Optional<NotificationTemplate> passkeyOtp = templateRepository.findTemplateById("passkey-otp");
        Optional<NotificationTemplate> passwordReset = templateRepository.findTemplateById("password-reset");
        Optional<NotificationTemplate> welcome = templateRepository.findTemplateById("welcome");
        Optional<NotificationTemplate> studyApproved = templateRepository.findTemplateById("study-approved");

        // Then
        assertThat(passkeyOtp).isPresent();
        assertThat(passwordReset).isPresent();
        assertThat(welcome).isPresent();
        assertThat(studyApproved).isPresent();
        
        // 모든 템플릿이 활성 상태인지 확인
        assertThat(passkeyOtp.get().isActive()).isTrue();
        assertThat(passwordReset.get().isActive()).isTrue();
        assertThat(welcome.get().isActive()).isTrue();
        assertThat(studyApproved.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("패스키 OTP 템플릿이 올바르게 설정된다")
    void passkeyOtpTemplate_isConfiguredCorrectly() {
        // When
        Optional<NotificationTemplate> template = templateRepository.findTemplateById("passkey-otp");

        // Then
        assertThat(template).isPresent();
        NotificationTemplate otp = template.get();
        
        assertThat(otp.getTitleTemplate()).isEqualTo("[AsyncSite] 패스키 인증 코드: {code}");
        assertThat(otp.getChannelType().name()).isEqualTo("EMAIL");
        assertThat(otp.getEventType().name()).isEqualTo("ACTION");
        assertThat(otp.getVariables()).containsKeys("code", "expiryMinutes");
        assertThat(otp.getContentTemplate())
            .contains("패스키 등록을 위한 인증 코드입니다")
            .contains("{code}")
            .contains("{expiryMinutes}분");
    }

    @Test
    @DisplayName("비밀번호 재설정 템플릿이 올바르게 설정된다")
    void passwordResetTemplate_isConfiguredCorrectly() {
        // When
        Optional<NotificationTemplate> template = templateRepository.findTemplateById("password-reset");

        // Then
        assertThat(template).isPresent();
        NotificationTemplate reset = template.get();
        
        assertThat(reset.getTitleTemplate()).isEqualTo("[AsyncSite] 비밀번호 재설정 안내");
        assertThat(reset.getChannelType().name()).isEqualTo("EMAIL");
        assertThat(reset.getEventType().name()).isEqualTo("PASSWORD_RESET");
        assertThat(reset.getVariables()).containsKeys("userName", "resetUrl");
        assertThat(reset.getContentTemplate())
            .contains("비밀번호 재설정을 요청하셨습니다")
            .contains("비밀번호 재설정하기")
            .contains("{resetUrl}");
    }

    @Test
    @DisplayName("환영 이메일 템플릿이 올바르게 설정된다")
    void welcomeTemplate_isConfiguredCorrectly() {
        // When
        Optional<NotificationTemplate> template = templateRepository.findTemplateById("welcome");

        // Then
        assertThat(template).isPresent();
        NotificationTemplate welcome = template.get();
        
        assertThat(welcome.getTitleTemplate()).contains("환영합니다");
        assertThat(welcome.getChannelType().name()).isEqualTo("EMAIL");
        assertThat(welcome.getEventType().name()).isEqualTo("NOTI");
        assertThat(welcome.getVariables()).containsKey("userName");
        assertThat(welcome.getContentTemplate())
            .contains("Welcome to AsyncSite!")
            .contains("다양한 스터디 참여")
            .contains("지식 공유");
    }

    @Test
    @DisplayName("스터디 승인 템플릿이 올바르게 설정된다")
    void studyApprovedTemplate_isConfiguredCorrectly() {
        // When
        Optional<NotificationTemplate> template = templateRepository.findTemplateById("study-approved");

        // Then
        assertThat(template).isPresent();
        NotificationTemplate approved = template.get();
        
        assertThat(approved.getTitleTemplate()).contains("승인되었습니다");
        assertThat(approved.getChannelType().name()).isEqualTo("EMAIL");
        assertThat(approved.getEventType().name()).isEqualTo("STUDY");
        assertThat(approved.getVariables()).containsKeys(
            "proposerName", "studyTitle", "studyId", 
            "startDate", "endDate", "maxMembers", "category"
        );
        assertThat(approved.getContentTemplate())
            .contains("스터디 승인 완료")
            .contains("다음 단계");
    }

    @Test
    @DisplayName("템플릿 렌더링이 변수를 올바르게 치환한다")
    void templateRendering_replacesVariablesCorrectly() {
        // Given
        Optional<NotificationTemplate> template = templateRepository.findTemplateById("passkey-otp");
        assertThat(template).isPresent();

        // When
        String renderedTitle = template.get().renderTitle(
            java.util.Map.of("code", "987654")
        );
        String renderedContent = template.get().renderContent(
            java.util.Map.of(
                "code", "987654",
                "expiryMinutes", "3"
            )
        );

        // Then
        assertThat(renderedTitle).isEqualTo("[AsyncSite] 패스키 인증 코드: 987654");
        assertThat(renderedContent)
            .contains("987654")
            .contains("3분");
    }

    @Test
    @DisplayName("템플릿 재실행 시 업데이트되지 않는다 (멱등성)")
    void templateInitialization_isIdempotent() throws Exception {
        // Given: 템플릿이 이미 로드된 상태
        Optional<NotificationTemplate> before = templateRepository.findTemplateById("passkey-otp");
        assertThat(before).isPresent();
        
        // When: 다시 초기화 실행
        systemTemplateInitializer.run(null);
        
        // Then: 템플릿이 그대로 유지됨
        Optional<NotificationTemplate> after = templateRepository.findTemplateById("passkey-otp");
        assertThat(after).isPresent();
        assertThat(after.get().getTitleTemplate()).isEqualTo(before.get().getTitleTemplate());
    }
}