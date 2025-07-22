package com.asyncsite.notiservice.adapter.in.web;

import com.asyncsite.notiservice.adapter.in.dto.NotificationTemplateRequest;
import com.asyncsite.notiservice.adapter.in.dto.NotificationTemplateResponse;
import com.asyncsite.notiservice.adapter.in.mapper.NotificationMapper;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
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
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String channelType,
            @RequestParam(required = false) String language,
            @RequestParam(required = false, defaultValue = "true") Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("템플릿 목록 조회: eventType={}, channelType={}, language={}, isActive={}, page={}, size={}",
                eventType, channelType, language, isActive, page, size);

        List<NotificationTemplate> templates = notificationTemplateUseCase.getTemplates(
                eventType, channelType, language, isActive, page, size);

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
            @Valid @RequestBody NotificationTemplateRequest request) {

        log.info("템플릿 생성 요청: eventType={}, channelType={}, language={}",
                request.eventType(), request.channelType(), request.language());

        // Mapper를 사용하여 Request에서 Domain 객체로 변환
        NotificationTemplate template = NotificationMapper.toNotificationTemplate(request);
        NotificationTemplate createdTemplate = notificationTemplateUseCase.createTemplate(template);
        NotificationTemplateResponse response = NotificationTemplateResponse.from(createdTemplate);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{templateId}")
    public ResponseEntity<NotificationTemplateResponse> updateTemplate(
            @PathVariable String templateId,
            @Valid @RequestBody NotificationTemplateRequest request) {

        log.info("템플릿 수정 요청: templateId={}", templateId);

        // 기존 템플릿 조회 후 업데이트
        return notificationTemplateUseCase.getTemplateById(templateId)
                .map(existingTemplate -> {
                    NotificationTemplate updatedTemplate = NotificationMapper.updateNotificationTemplate(existingTemplate, request);
                    NotificationTemplate savedTemplate = notificationTemplateUseCase.updateTemplate(templateId, updatedTemplate);
                    NotificationTemplateResponse response = NotificationTemplateResponse.from(savedTemplate);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{templateId}/deactivate")
    public ResponseEntity<Void> deactivateTemplate(
            @PathVariable String templateId) {

        log.info("템플릿 비활성화 요청: templateId={}", templateId);

        notificationTemplateUseCase.deactivateTemplate(templateId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{templateId}/clone")
    public ResponseEntity<NotificationTemplateResponse> cloneTemplate(
            @PathVariable String templateId,
            @Valid @RequestBody NotificationTemplateRequest request) {

        log.info("템플릿 복제 요청: templateId={}, language={}", templateId, request.language());

        NotificationTemplate clonedTemplate = notificationTemplateUseCase.cloneTemplate(
                templateId, 
                request.language(), 
                request.titleTemplate(), 
                request.contentTemplate()
        );
        NotificationTemplateResponse response = NotificationTemplateResponse.from(clonedTemplate);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
