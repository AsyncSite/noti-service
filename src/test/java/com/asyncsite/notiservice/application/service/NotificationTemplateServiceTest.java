package com.asyncsite.notiservice.application.service;

import com.asyncsite.notiservice.domain.model.NotificationChannel;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.port.out.NotificationTemplateRepositoryPort;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

class NotificationTemplateServiceTest {
    @Mock
    private NotificationTemplateRepositoryPort templateRepository;

    @InjectMocks
    private NotificationTemplateService templateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getTemplates() {
        NotificationTemplate t1 = NotificationTemplate.builder().templateId("1L").eventType("A").channelType(NotificationChannel.ChannelType.EMAIL).language("ko").titleTemplate("").contentTemplate("").variables(Map.of()).active(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        given(templateRepository.findTemplatesByFilters("A", NotificationChannel.ChannelType.EMAIL, "ko", true, 0, 10)).willReturn(List.of(t1));
        List<NotificationTemplate> result = templateService.getTemplates("A", "EMAIL", "ko", true, 0, 10);
        assertThat(result).hasSize(1);
    }

    @Test
    void getTemplateById() {
        NotificationTemplate t1 = NotificationTemplate.builder().templateId("1L").eventType("A").channelType(NotificationChannel.ChannelType.EMAIL).language("ko").titleTemplate("").contentTemplate("").variables(Map.of()).active(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        given(templateRepository.findTemplateById("1L")).willReturn(Optional.of(t1));
        Optional<NotificationTemplate> result = templateService.getTemplateById("1L");
        assertThat(result).isPresent();
        assertThat(result.get().getTemplateId()).isEqualTo("1L");
    }

    @Test
    void getTemplateByEventAndChannel() {
        NotificationTemplate t1 = NotificationTemplate.builder().templateId("1L").eventType("A").channelType(NotificationChannel.ChannelType.EMAIL).language("ko").titleTemplate("").contentTemplate("").variables(Map.of()).active(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        given(templateRepository.findTemplateByEventAndChannel("A", NotificationChannel.ChannelType.EMAIL, "ko")).willReturn(Optional.of(t1));
        Optional<NotificationTemplate> result = templateService.getTemplateByEventAndChannel("A", "EMAIL", "ko");
        assertThat(result).isPresent();
    }

    @Test
    void createTemplate() {
        NotificationTemplate t1 = NotificationTemplate.builder().eventType("A").channelType(NotificationChannel.ChannelType.EMAIL).language("ko").titleTemplate("").contentTemplate("").variables(Map.of()).active(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        given(templateRepository.saveTemplate(ArgumentMatchers.any())).willReturn(t1);
        NotificationTemplate result = templateService.createTemplate(t1);
        assertThat(result).isNotNull();
    }

    @Test
    void updateTemplate() {
        NotificationTemplate t1 = NotificationTemplate.builder().templateId("1L").eventType("A").channelType(NotificationChannel.ChannelType.EMAIL).language("ko").titleTemplate("").contentTemplate("").variables(Map.of()).active(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        given(templateRepository.findTemplateById("1L")).willReturn(Optional.of(t1));
        given(templateRepository.saveTemplate(ArgumentMatchers.any())).willReturn(t1);
        NotificationTemplate result = templateService.updateTemplate("1L", t1);
        assertThat(result).isNotNull();
    }

    @Test
    void deactivateTemplate() {
        NotificationTemplate t1 = NotificationTemplate.builder().templateId("1L").eventType("A").channelType(NotificationChannel.ChannelType.EMAIL).language("ko").titleTemplate("").contentTemplate("").variables(Map.of()).active(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        given(templateRepository.findTemplateById("1L")).willReturn(Optional.of(t1));
        given(templateRepository.saveTemplate(ArgumentMatchers.any())).willReturn(t1.toBuilder().active(false).build());
        templateService.deactivateTemplate("1L");
    }

    @Test
    void cloneTemplate() {
        NotificationTemplate t1 = NotificationTemplate.builder().templateId("1L").eventType("A").channelType(NotificationChannel.ChannelType.EMAIL).language("ko").titleTemplate("T").contentTemplate("C").variables(Map.of()).active(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        given(templateRepository.findTemplateById("1L")).willReturn(Optional.of(t1));
        given(templateRepository.saveTemplate(ArgumentMatchers.any())).willReturn(t1.toBuilder().templateId("2L").language("en").build());
        NotificationTemplate result = templateService.cloneTemplate("1L", "en", "T2", "C2");
        assertThat(result.getTemplateId()).isEqualTo("2L");
        assertThat(result.getLanguage()).isEqualTo("en");
    }

    @Test
    void previewTemplate() {
        NotificationTemplate t1 = NotificationTemplate.builder().templateId("1L").eventType("A").channelType(NotificationChannel.ChannelType.EMAIL).language("ko").titleTemplate("[{{user}}] 알림").contentTemplate("{{user}}님, 테스트").variables(Map.of("user", "홍길동")).active(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        given(templateRepository.findTemplateById("1L")).willReturn(Optional.of(t1));
        Map<String, String> preview = templateService.previewTemplate("1L", Map.of("user", "홍길동"));
        assertThat(preview.get("title")).isEqualTo("[홍길동] 알림");
        assertThat(preview.get("content")).isEqualTo("홍길동님, 테스트");
    }
}
