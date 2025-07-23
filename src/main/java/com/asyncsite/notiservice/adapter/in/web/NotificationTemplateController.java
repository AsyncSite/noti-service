package com.asyncsite.notiservice.adapter.in.web;

import com.asyncsite.notiservice.adapter.in.dto.CreateNotificationTemplateRequest;
import com.asyncsite.notiservice.adapter.in.dto.UpdateNotificationTemplateRequest;
import com.asyncsite.notiservice.adapter.in.dto.NotificationTemplateResponse;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.port.in.NotificationTemplateUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/notification-templates")
@RequiredArgsConstructor
public class NotificationTemplateController {

    private final NotificationTemplateUseCase notificationTemplateUseCase;

    @GetMapping
    public ResponseEntity<Page<NotificationTemplateResponse>> getTemplates(
            @RequestParam(required = false) String channelType,
            @RequestParam(required = false, defaultValue = "true") Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {


        List<NotificationTemplate> templates = notificationTemplateUseCase.getTemplates(
                ChannelType.valueOf(channelType), active, page, size);

        List<NotificationTemplateResponse> responses = templates.stream()
                .map(NotificationTemplateResponse::from)
                .toList();
        PageImpl<NotificationTemplateResponse> pageImpl = new PageImpl<>(responses);
        return ResponseEntity.ok(pageImpl);
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<NotificationTemplateResponse> getTemplate(
            @PathVariable String templateId) {

        log.info("템플릿 상세 조회: templateId={}", templateId);

        return notificationTemplateUseCase.getTemplateById(templateId)
                .map(template -> ResponseEntity.ok(NotificationTemplateResponse.from(template)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<NotificationTemplateResponse> createTemplate(
            @Valid @RequestBody CreateNotificationTemplateRequest request) {
        // Mapper를 사용하여 Request에서 Domain 객체로 변환
        return ResponseEntity.status(HttpStatus.CREATED).body(NotificationTemplateResponse.from(notificationTemplateUseCase.createTemplate(request.channelType(),  request.titleTemplate(), request.contentTemplate(), request.variables())));
    }

    @PutMapping("/{templateId}")
    public ResponseEntity<NotificationTemplateResponse> updateTemplate(
            @PathVariable String templateId,
            @Valid @RequestBody UpdateNotificationTemplateRequest request) {

        log.info("템플릿 수정 요청: templateId={}", templateId);

        // 기존 템플릿 조회 후 업데이트
        return ResponseEntity.ok(NotificationTemplateResponse.from(notificationTemplateUseCase.updateTemplate(templateId, request.titleTemplate(), request.contentTemplate(), request.variables())));
    }

    @PatchMapping("/{templateId}/deactivate")
    public ResponseEntity<Void> deactivateTemplate(
            @PathVariable String templateId) {

        log.info("템플릿 비활성화 요청: templateId={}", templateId);

        notificationTemplateUseCase.deactivateTemplate(templateId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{templateId}/preview")
    public ResponseEntity<Map<String, String>> previewTemplate(
            @PathVariable String templateId,
            @RequestBody Map<String, Object> variables) {

        log.info("템플릿 미리보기 요청: templateId={}", templateId);

        Map<String, String> preview = notificationTemplateUseCase.previewTemplate(templateId, variables);
        return ResponseEntity.ok(preview);
    }
}
