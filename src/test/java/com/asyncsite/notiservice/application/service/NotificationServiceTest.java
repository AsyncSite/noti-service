package com.asyncsite.notiservice.application.service;

import com.asyncsite.notiservice.domain.model.Notification;
import com.asyncsite.notiservice.domain.port.out.NotificationRepositoryPort;
import com.asyncsite.notiservice.domain.port.out.NotificationSettingsRepositoryPort;
import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
import com.asyncsite.notiservice.domain.port.out.NotificationSenderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

class NotificationServiceTest {
    @Mock
    private NotificationRepositoryPort notificationRepository;
    @Mock
    private NotificationSettingsRepositoryPort settingsRepository;
    @Mock
    private NotificationTemplateRepositoryPort templateRepository;
    @Mock
    private List<NotificationSenderPort> notificationSenders;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getNotificationById() {
        Notification notification = Notification.builder()
                .notificationId("1L")
                .userId("100")
                .eventType("STUDY_UPDATE")
                .title("제목")
                .content("내용")
                .status(Notification.NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .retryCount(0)
                .build();
        given(notificationRepository.findNotificationById("1L")).willReturn(Optional.of(notification));
        Optional<Notification> result = notificationService.getNotificationById("1L");
        assertThat(result).isPresent();
        assertThat(result.get().getNotificationId()).isEqualTo("1L");
    }

    @Test
    void getNotificationById_notFound() {
        given(notificationRepository.findNotificationById("2L")).willReturn(Optional.empty());
        Optional<Notification> result = notificationService.getNotificationById("2L");
        assertThat(result).isEmpty();
    }

    @Test
    void getNotificationsByUserId() {
        Notification n1 = Notification.builder().notificationId("1L").userId("100").eventType("A").title("").content("").status(Notification.NotificationStatus.PENDING).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).retryCount(0).build();
        Notification n2 = Notification.builder().notificationId("2L").userId("100").eventType("B").title("").content("").status(Notification.NotificationStatus.PENDING).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).retryCount(0).build();
        given(notificationRepository.findNotificationsByUserId("100", 0, 10)).willReturn(List.of(n1, n2));
        List<Notification> result = notificationService.getNotificationsByUserId("100", 0, 10);
        assertThat(result).hasSize(2);
    }

    @Test
    void sendNotification_success() throws Exception {
        given(settingsRepository.findByUserId("100")).willReturn(
                com.asyncsite.notiservice.domain.model.NotificationSettings.builder()
                        .userId("100")
                        .studyUpdates(true)
                        .marketing(false)
                        .emailEnabled(true)
                        .discordEnabled(false)
                        .pushEnabled(false)
                        .timezone("Asia/Seoul")
                        .language("ko")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );
        given(templateRepository.findTemplateByEventAndChannel(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .willReturn(Optional.of(com.asyncsite.notiservice.domain.model.NotificationTemplate.builder()
                        .templateId("1L")
                        .eventType("STUDY_UPDATE")
                        .channelType(com.asyncsite.notiservice.domain.model.NotificationChannel.ChannelType.EMAIL)
                        .language("ko")
                        .titleTemplate("[{{user}}] 스터디 알림")
                        .contentTemplate("{{user}}님, 스터디가 곧 시작됩니다.")
                        .variables(Map.of("user", "홍길동"))
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
                ));
        given(notificationRepository.saveNotification(ArgumentMatchers.any())).willAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            return n.toBuilder().notificationId("1L").build();
        });
        CompletableFuture<Notification> future = notificationService.sendNotification("100", "STUDY_UPDATE", Map.of("user", "홍길동"));
        Notification result = future.get();
        assertThat(result.getNotificationId()).isNotNull();
        assertThat(result.getUserId()).isEqualTo("100");
    }
}
