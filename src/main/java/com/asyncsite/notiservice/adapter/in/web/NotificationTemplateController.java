package com.asyncsite.notiservice.adapter.in.web;

import com.asyncsite.coreplatform.common.dto.ApiResponse;
import com.asyncsite.notiservice.adapter.in.web.dto.CreateNotificationTemplateRequest;
import com.asyncsite.notiservice.adapter.in.web.dto.NotificationTemplateResponse;
import com.asyncsite.notiservice.adapter.in.web.dto.UpdateNotificationTemplateRequest;
import com.asyncsite.notiservice.domain.model.NotificationTemplate;
import com.asyncsite.notiservice.domain.port.in.NotificationTemplateUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/noti/templates")
@RequiredArgsConstructor
public class NotificationTemplateController {

    private final NotificationTemplateUseCase templateUseCase;

    @GetMapping
    public ApiResponse<List<NotificationTemplateResponse>> getTemplates(
            @RequestParam(required = false) String channelType,
            @RequestParam(required = false, defaultValue = "true") Boolean active) {


        List<NotificationTemplate> templates = templateUseCase.getTemplates(
                channelType, active);

        List<NotificationTemplateResponse> responses = templates.stream()
                .map(NotificationTemplateResponse::from)
                .toList();
        return ApiResponse.success(responses);
    }

    @GetMapping("/{templateId}")
    public ApiResponse<NotificationTemplateResponse> getTemplate(
            @PathVariable String templateId) {

        log.info("템플릿 상세 조회: templateId={}", templateId);

        return templateUseCase.getTemplateById(templateId)
                .map(template -> ApiResponse.success(NotificationTemplateResponse.from(template)))
                .get();
    }

    @PostMapping
    public ApiResponse<NotificationTemplateResponse> createTemplate(
            @Valid @RequestBody CreateNotificationTemplateRequest request) {
        // Mapper를 사용하여 Request에서 Domain 객체로 변환
        return ApiResponse.success(NotificationTemplateResponse.from(templateUseCase.createTemplate(request.channelType(), request.eventType(), request.titleTemplate(), request.contentTemplate(), request.variables())));
    }

    @PutMapping("/{templateId}")
    public ApiResponse<NotificationTemplateResponse> updateTemplate(
            @PathVariable String templateId,
            @Valid @RequestBody UpdateNotificationTemplateRequest request) {

        log.info("템플릿 수정 요청: templateId={}", templateId);

        // 기존 템플릿 조회 후 업데이트
        return ApiResponse.success(NotificationTemplateResponse.from(templateUseCase.updateTemplate(templateId, request.titleTemplate(), request.contentTemplate(), request.variables())));
    }

    @PatchMapping("/{templateId}/deactivate")
    public ApiResponse<Void> deactivateTemplate(
            @PathVariable String templateId) {

        log.info("템플릿 비활성화 요청: templateId={}", templateId);

        templateUseCase.deactivateTemplate(templateId);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{templateId}/default")
    public ApiResponse<Void> setDefault(@PathVariable String templateId) {
        templateUseCase.setDefaultTemplate(templateId);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{templateId}/priority")
    public ApiResponse<Void> updatePriority(
            @PathVariable String templateId,
            @RequestParam int value) {
        templateUseCase.updatePriority(templateId, value);
        return ApiResponse.success(null);
    }

    // FIXME 작업중
    @PostMapping("/{templateId}/preview")
    public ApiResponse<Map<String, String>> previewTemplate(
            @PathVariable String templateId,
            @RequestBody Map<String, Object> variables) {

        log.info("템플릿 미리보기 요청: templateId={}", templateId);

        Map<String, String> preview = templateUseCase.previewTemplate(templateId, variables);
        return ApiResponse.success(preview);
    }
}
