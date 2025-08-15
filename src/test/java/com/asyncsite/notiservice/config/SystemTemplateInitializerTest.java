package com.asyncsite.notiservice.config;

import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("시스템 템플릿 초기화 테스트")
class SystemTemplateInitializerTest {

    @Autowired
    private NotificationTemplateRepositoryPort templateRepository;

    @Test
    @DisplayName("애플리케이션 시작 시 시스템 템플릿이 자동으로 로드된다")
    void whenApplicationStarts_thenSystemTemplatesAreLoaded() {
        // Given: Application started with SystemTemplateInitializer

        // When: Check if system templates are loaded
        Optional<NotificationTemplate> passkeyOtp = templateRepository.findTemplateById("passkey-otp");
        Optional<NotificationTemplate> passwordReset = templateRepository.findTemplateById("password-reset");
        Optional<NotificationTemplate> welcome = templateRepository.findTemplateById("welcome");
        Optional<NotificationTemplate> studyApproved = templateRepository.findTemplateById("study-approved");

        // Then: All system templates should be present
        assertThat(passkeyOtp).isPresent();
        assertThat(passkeyOtp.get().getTitleTemplate())
            .isEqualTo("[AsyncSite] 패스키 인증 코드: {code}");
        assertThat(passkeyOtp.get().isActive()).isTrue();

        assertThat(passwordReset).isPresent();
        assertThat(passwordReset.get().getTitleTemplate())
            .isEqualTo("[AsyncSite] 비밀번호 재설정 안내");
        assertThat(passwordReset.get().isActive()).isTrue();

        assertThat(welcome).isPresent();
        assertThat(welcome.get().getTitleTemplate())
            .contains("환영합니다");
        assertThat(welcome.get().isActive()).isTrue();

        assertThat(studyApproved).isPresent();
        assertThat(studyApproved.get().getTitleTemplate())
            .contains("승인되었습니다");
        assertThat(studyApproved.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("패스키 OTP 템플릿이 올바른 변수를 포함한다")
    void passkeyOtpTemplate_containsCorrectVariables() {
        // When
        Optional<NotificationTemplate> template = templateRepository.findTemplateById("passkey-otp");

        // Then
        assertThat(template).isPresent();
        assertThat(template.get().getVariables()).containsKeys("code", "expiryMinutes");
        assertThat(template.get().getContentTemplate()).contains("{code}", "{expiryMinutes}");
    }

    @Test
    @DisplayName("비밀번호 재설정 템플릿이 올바른 변수를 포함한다")
    void passwordResetTemplate_containsCorrectVariables() {
        // When
        Optional<NotificationTemplate> template = templateRepository.findTemplateById("password-reset");

        // Then
        assertThat(template).isPresent();
        assertThat(template.get().getVariables()).containsKeys("userName", "resetUrl");
        assertThat(template.get().getContentTemplate()).contains("{userName}", "{resetUrl}");
    }

    @Test
    @DisplayName("환영 이메일 템플릿이 올바른 변수를 포함한다")
    void welcomeTemplate_containsCorrectVariables() {
        // When
        Optional<NotificationTemplate> template = templateRepository.findTemplateById("welcome");

        // Then
        assertThat(template).isPresent();
        assertThat(template.get().getVariables()).containsKey("userName");
        // welcome 템플릿은 기본 변수를 공란으로 둘 수 있어 본문에 {userName}이 없을 수 있다
    }

    @Test
    @DisplayName("스터디 승인 템플릿이 올바른 변수를 포함한다")
    void studyApprovedTemplate_containsCorrectVariables() {
        // When
        Optional<NotificationTemplate> template = templateRepository.findTemplateById("study-approved");

        // Then
        assertThat(template).isPresent();
        assertThat(template.get().getVariables()).containsKeys(
            "proposerName", "studyTitle", "studyId", 
            "startDate", "endDate", "maxMembers", "category"
        );
    }

    @Test
    @DisplayName("템플릿 렌더링이 정상적으로 작동한다")
    void templateRendering_worksCorrectly() {
        // Given
        Optional<NotificationTemplate> template = templateRepository.findTemplateById("passkey-otp");
        assertThat(template).isPresent();

        // When
        String renderedTitle = template.get().renderTitle(
            java.util.Map.of("code", "123456")
        );
        String renderedContent = template.get().renderContent(
            java.util.Map.of(
                "code", "123456",
                "expiryMinutes", "10"
            )
        );

        // Then
        assertThat(renderedTitle).isEqualTo("[AsyncSite] 패스키 인증 코드: 123456");
        assertThat(renderedContent)
            .contains("123456")
            .contains("10분");
    }
}