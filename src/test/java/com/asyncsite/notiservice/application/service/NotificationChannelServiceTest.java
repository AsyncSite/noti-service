package com.asyncsite.notiservice.application.service;

import com.asyncsite.notiservice.domain.model.NotificationChannel;
import com.asyncsite.notiservice.domain.port.out.NotificationChannelRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

class NotificationChannelServiceTest {
    @Mock
    private NotificationChannelRepositoryPort channelRepository;

    @InjectMocks
    private NotificationChannelService channelService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getChannelsByNotificationId() {
        NotificationChannel c1 = NotificationChannel.builder().channelId("1L").notificationId("10L").channelType(NotificationChannel.ChannelType.EMAIL).recipient("a").status(NotificationChannel.Status.PENDING).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).retryCount(0).build();
        NotificationChannel c2 = NotificationChannel.builder().channelId("2L").notificationId("10L").channelType(NotificationChannel.ChannelType.PUSH).recipient("b").status(NotificationChannel.Status.PENDING).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).retryCount(0).build();
        given(channelRepository.findByNotificationId("10L")).willReturn(List.of(c1, c2));
        List<NotificationChannel> result = channelService.getChannelsByNotificationId("10L", null);
        assertThat(result).hasSize(2);
    }

    @Test
    void getChannelsByNotificationId_withType() {
        NotificationChannel c1 = NotificationChannel.builder().channelId("1L").notificationId("10L").channelType(NotificationChannel.ChannelType.EMAIL).recipient("a").status(NotificationChannel.Status.PENDING).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).retryCount(0).build();
        NotificationChannel c2 = NotificationChannel.builder().channelId("2L").notificationId("10L").channelType(NotificationChannel.ChannelType.PUSH).recipient("b").status(NotificationChannel.Status.PENDING).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).retryCount(0).build();
        given(channelRepository.findByNotificationId("10L")).willReturn(List.of(c1, c2));
        List<NotificationChannel> result = channelService.getChannelsByNotificationId("10L", "EMAIL");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChannelType()).isEqualTo(NotificationChannel.ChannelType.EMAIL);
    }

    @Test
    void getChannelById() {
        NotificationChannel c1 = NotificationChannel.builder().channelId("1L").notificationId("10L").channelType(NotificationChannel.ChannelType.EMAIL).recipient("a").status(NotificationChannel.Status.PENDING).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).retryCount(0).build();
        given(channelRepository.findById("1L")).willReturn(Optional.of(c1));
        NotificationChannel result = channelService.getChannelById("1L");
        assertThat(result.getChannelId()).isEqualTo("1L");
    }

    @Test
    void retryChannel() {
        NotificationChannel c1 = NotificationChannel.builder().channelId("1L").notificationId("10L").channelType(NotificationChannel.ChannelType.EMAIL).recipient("a").status(NotificationChannel.Status.FAILED).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).retryCount(1).build();
        given(channelRepository.findById("1L")).willReturn(Optional.of(c1));
        given(channelRepository.save(ArgumentMatchers.any())).willAnswer(invocation -> invocation.getArgument(0));
        NotificationChannel result = channelService.retryChannel("1L");
        assertThat(result.getRetryCount()).isEqualTo(2);
        assertThat(result.getStatus()).isEqualTo(NotificationChannel.Status.PENDING);
    }
}
