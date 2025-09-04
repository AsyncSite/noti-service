package com.asyncsite.notiservice.adapter.in.event;

import com.asyncsite.notiservice.adapter.in.event.dto.DocumentoParsedEvent;
import com.asyncsite.notiservice.domain.model.vo.ChannelType;
import com.asyncsite.notiservice.domain.model.vo.EventType;
import com.asyncsite.notiservice.domain.port.in.NotificationUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Kafka listener for documento parsing completion events.
 * Consumes events from parser-worker and creates notifications for email delivery.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentoParsedEventListener {
    
    private final NotificationUseCase notificationUseCase;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(
        topics = "${kafka.topics.documento-parsed:asyncsite.documento.content.parsed}",
        groupId = "${spring.application.name}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDocumentoParsed(
            @Payload JsonNode eventNode,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = "correlationId", required = false) String correlationId,
            Acknowledgment acknowledgment) {
        
        // Set correlation ID in MDC for tracking
        if (correlationId != null) {
            MDC.put("correlationId", correlationId);
        }
        
        try {
            // Parse the event from JsonNode
            DocumentoParsedEvent event = objectMapper.treeToValue(eventNode, DocumentoParsedEvent.class);
            
            log.info("[KAFKA] Received DocumentoParsedEvent - EventId: {}, ContentId: {}, User: {}, Topic: {}, Partition: {}, Offset: {}, CorrelationId: {}",
                    event.getEventId(), event.getContentId(), event.getUserEmail(), topic, partition, offset, correlationId);
            
            // Build metadata for notification
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("templateId", "documento-analysis");
            
            // Convert analysis result to template variables (pass JsonNode for extra fields)
            Map<String, Object> variables = buildTemplateVariables(event, eventNode);
            metadata.put("variables", variables);
            
            // Additional context
            if (correlationId != null) {
                metadata.put("correlationId", correlationId);
            }
            metadata.put("eventId", event.getEventId());
            metadata.put("contentId", event.getContentId());
            
            // Create notification using existing NotificationUseCase
            // For trial users, pass email as recipientContact
            // For authenticated users, pass userId and email
            String effectiveUserId = event.isTrialUser() ? null : event.getUserId();
            
            notificationUseCase.createNotification(
                effectiveUserId,
                ChannelType.EMAIL,
                EventType.DOCUMENTO_ANALYZED,
                metadata,
                event.getUserEmail()
            );
            
            log.info("[KAFKA] Successfully processed DocumentoParsedEvent for content: {} and user: {}", 
                    event.getContentId(), event.getUserEmail());
            
            // Acknowledge the message
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            
        } catch (Exception e) {
            log.error("[KAFKA] Failed to process DocumentoParsedEvent - Error: {}", 
                    e.getMessage(), e);
            
            // Don't acknowledge on error - message will be retried
            throw new RuntimeException("Failed to process DocumentoParsedEvent", e);
        } finally {
            // Clean up MDC after processing
            MDC.remove("correlationId");
        }
    }
    
    private Map<String, Object> buildTemplateVariables(DocumentoParsedEvent event, JsonNode eventNode) {
        Map<String, Object> variables = new HashMap<>();
        
        // Basic information
        variables.put("userName", extractUserName(event.getUserEmail()));
        variables.put("title", event.getTitle());
        variables.put("documentoId", event.getContentId());
        variables.put("summary", event.getSummary());
        
        // Dynamic category ratings processing - CRITICAL FIX
        // Process ALL categories from Parser Worker dynamically
        if (event.getAnalysisResult() != null && event.getAnalysisResult().getCategoryRatings() != null) {
            List<DocumentoParsedEvent.CategoryRating> ratings = event.getAnalysisResult().getCategoryRatings();
            
            // Create dynamic category list for template iteration
            List<Map<String, Object>> categoryList = new ArrayList<>();
            
            // Legacy mappings for backward compatibility
            Map<String, String> legacyMappings = new HashMap<>();
            legacyMappings.put("제목 매력도", "titleRating");
            legacyMappings.put("첫인상", "firstImpressionRating");
            legacyMappings.put("가독성", "readabilityRating");
            legacyMappings.put("구조/흐름", "structureRating");
            legacyMappings.put("감정 전달", "emotionalRating");
            
            // Process all categories dynamically
            for (DocumentoParsedEvent.CategoryRating rating : ratings) {
                if (rating != null && rating.getCategory() != null) {
                    String category = rating.getCategory();
                    String comment = rating.getComment() != null ? rating.getComment() : "";
                    Integer ratingValue = rating.getRating();
                    String stars = generateStars(ratingValue);
                    
                    // Add to dynamic list (for ALL categories)
                    Map<String, Object> categoryItem = new HashMap<>();
                    categoryItem.put("name", category);
                    categoryItem.put("stars", stars);
                    categoryItem.put("rating", ratingValue != null ? ratingValue : 0);
                    categoryItem.put("comment", comment);
                    categoryList.add(categoryItem);
                    
                    // Maintain legacy variables for backward compatibility
                    String legacyKey = legacyMappings.get(category);
                    if (legacyKey != null) {
                        variables.put(legacyKey, stars);
                        variables.put(legacyKey.replace("Rating", "Comment"), comment);
                    }
                }
            }
            
            // Add dynamic category list for template iteration
            variables.put("categoryRatings", categoryList);
            
            // Generate HTML table for all categories dynamically
            StringBuilder categoryTableHtml = new StringBuilder();
            if (categoryList.isEmpty()) {
                // No categories available
                categoryTableHtml.append(
                    "<tr>" +
                    "<td colspan=\"3\" style=\"padding: 20px; text-align: center; color: #666;\">" +
                    "카테고리별 평점이 아직 준비되지 않았습니다." +
                    "</td>" +
                    "</tr>"
                );
            } else {
                // Generate rows for each category
                for (int i = 0; i < categoryList.size(); i++) {
                    Map<String, Object> category = categoryList.get(i);
                    String borderStyle = (i == categoryList.size() - 1) ? "" : " border-bottom: 1px solid #dee2e6;";
                    categoryTableHtml.append(String.format(
                        "<tr>" +
                        "<td style=\"padding: 12px;%s\"><strong>%s</strong></td>" +
                        "<td style=\"padding: 12px;%s text-align: center;\">" +
                        "<span style=\"color: #ffc107;\">%s</span>" +
                        "</td>" +
                        "<td style=\"padding: 12px;%s font-size: 14px; color: #666;\">%s</td>" +
                        "</tr>",
                        borderStyle, escapeHtml(String.valueOf(category.get("name"))),
                        borderStyle, category.get("stars"),
                        borderStyle, escapeHtml(String.valueOf(category.get("comment")))
                    ));
                }
            }
            variables.put("categoryRatingsTable", categoryTableHtml.toString());
            
            // Set legacy defaults for missing mappings
            for (Map.Entry<String, String> entry : legacyMappings.entrySet()) {
                String legacyKey = entry.getValue();
                if (!variables.containsKey(legacyKey)) {
                    variables.put(legacyKey, "N/A");
                    variables.put(legacyKey.replace("Rating", "Comment"), "");
                }
            }
            
            // Overall score and assessment
            if (event.getAnalysisResult().getOverallScore() != null) {
                variables.put("overallScore", String.format("%.1f", event.getAnalysisResult().getOverallScore()));
            } else {
                variables.put("overallScore", "N/A");
            }
            
            if (event.getAnalysisResult().getOverallAssessment() != null) {
                variables.put("overallAssessment", event.getAnalysisResult().getOverallAssessment());
            } else {
                variables.put("overallAssessment", "전반적으로 잘 쓰셨어요! 👏");
            }
            
            // Add dynamic encouragement phrase
            if (event.getAnalysisResult().getEncouragementPhrase() != null) {
                variables.put("encouragementPhrase", event.getAnalysisResult().getEncouragementPhrase());
            } else {
                variables.put("encouragementPhrase", "몇 가지만 보완하면 정말 완벽한 글이 될 거예요!");
            }
        } else {
            // No analysis result - set empty category list and table
            variables.put("categoryRatings", new ArrayList<>());
            variables.put("categoryRatingsTable", 
                "<tr><td colspan=\"3\" style=\"padding: 20px; text-align: center; color: #666;\">" +
                "분석 결과가 없습니다.</td></tr>");
            variables.put("overallScore", "N/A");
            variables.put("overallAssessment", "분석 결과가 없습니다");
            variables.put("encouragementPhrase", "몇 가지만 보완하면 정말 완벽한 글이 될 거예요!");  // Default encouragement
            
            // Set all legacy variables to N/A
            variables.put("titleRating", "N/A");
            variables.put("titleComment", "");
            variables.put("firstImpressionRating", "N/A");
            variables.put("firstImpressionComment", "");
            variables.put("readabilityRating", "N/A");
            variables.put("readabilityComment", "");
            variables.put("structureRating", "N/A");
            variables.put("structureComment", "");
            variables.put("emotionalRating", "N/A");
            variables.put("emotionalComment", "");
        }
        
        // Detailed review sections
        if (event.getAnalysisResult() != null && event.getAnalysisResult().getDetailedReview() != null) {
            DocumentoParsedEvent.DetailedReview detailedReview = event.getAnalysisResult().getDetailedReview();
            
            // First paragraph review
            if (detailedReview.getFirstParagraph() != null) {
                DocumentoParsedEvent.ReviewSection first = detailedReview.getFirstParagraph();
                // New fullReview field for natural paragraph
                if (first.getFullReview() != null && !first.getFullReview().isEmpty()) {
                    variables.put("firstParagraphFullReview", first.getFullReview());
                } else {
                    // Fallback: construct from existing fields
                    String fallbackReview = String.format("<strong>첫 문단</strong>의 '%s' 부분이 %s. %s.",
                        first.getQuote() != null ? escapeHtml(first.getQuote()) : "내용",
                        first.getAnalysis() != null ? escapeHtml(first.getAnalysis()) : "분석되었습니다",
                        first.getSuggestion() != null ? escapeHtml(first.getSuggestion()) : "개선이 필요합니다"
                    );
                    variables.put("firstParagraphFullReview", fallbackReview);
                }
                // Keep existing fields for backward compatibility
                variables.put("firstParagraphQuote", escapeHtml(first.getQuote()));
                variables.put("firstParagraphAnalysis", escapeHtml(first.getAnalysis()));
                variables.put("firstParagraphSuggestion", escapeHtml(first.getSuggestion()));
            } else {
                variables.put("firstParagraphFullReview", "<strong>첫 문단</strong> 분석이 없습니다");
                variables.put("firstParagraphQuote", "");
                variables.put("firstParagraphAnalysis", "첫 문단 분석이 없습니다");
                variables.put("firstParagraphSuggestion", "");
            }
            
            // Middle section review
            if (detailedReview.getMiddleSection() != null) {
                DocumentoParsedEvent.ReviewSection middle = detailedReview.getMiddleSection();
                // New fullReview field for natural paragraph
                if (middle.getFullReview() != null && !middle.getFullReview().isEmpty()) {
                    variables.put("middleSectionFullReview", middle.getFullReview());
                } else {
                    // Fallback: construct from existing fields
                    String fallbackReview = String.format("<strong>중간 부분</strong>에서 '%s' 같은 %s. %s.",
                        middle.getQuote() != null ? escapeHtml(middle.getQuote()) : "내용",
                        middle.getAnalysis() != null ? escapeHtml(middle.getAnalysis()) : "부분이 있습니다",
                        middle.getSuggestion() != null ? escapeHtml(middle.getSuggestion()) : "개선이 필요합니다"
                    );
                    variables.put("middleSectionFullReview", fallbackReview);
                }
                // Keep existing fields for backward compatibility
                variables.put("middleSectionQuote", escapeHtml(middle.getQuote()));
                variables.put("middleSectionAnalysis", escapeHtml(middle.getAnalysis()));
                variables.put("middleSectionSuggestion", escapeHtml(middle.getSuggestion()));
            } else {
                variables.put("middleSectionFullReview", "<strong>중간 부분</strong> 분석이 없습니다");
                variables.put("middleSectionQuote", "");
                variables.put("middleSectionAnalysis", "중간 부분 분석이 없습니다");
                variables.put("middleSectionSuggestion", "");
            }
            
            // Conclusion review
            if (detailedReview.getConclusion() != null) {
                DocumentoParsedEvent.ReviewSection conclusion = detailedReview.getConclusion();
                // New fullReview field for natural paragraph
                if (conclusion.getFullReview() != null && !conclusion.getFullReview().isEmpty()) {
                    variables.put("conclusionFullReview", conclusion.getFullReview());
                } else {
                    // Fallback: construct from existing fields
                    String fallbackReview = String.format("<strong>마무리</strong>가 '%s' %s. %s.",
                        conclusion.getQuote() != null ? escapeHtml(conclusion.getQuote()) : "내용으로",
                        conclusion.getAnalysis() != null ? escapeHtml(conclusion.getAnalysis()) : "끝났습니다",
                        conclusion.getSuggestion() != null ? escapeHtml(conclusion.getSuggestion()) : "개선이 필요합니다"
                    );
                    variables.put("conclusionFullReview", fallbackReview);
                }
                // Keep existing fields for backward compatibility
                variables.put("conclusionQuote", escapeHtml(conclusion.getQuote()));
                variables.put("conclusionAnalysis", escapeHtml(conclusion.getAnalysis()));
                variables.put("conclusionSuggestion", escapeHtml(conclusion.getSuggestion()));
            } else {
                variables.put("conclusionFullReview", "<strong>마무리</strong> 분석이 없습니다");
                variables.put("conclusionQuote", "");
                variables.put("conclusionAnalysis", "마무리 분석이 없습니다");
                variables.put("conclusionSuggestion", "");
            }
            
            // Key advice
            if (detailedReview.getKeyAdvice() != null) {
                variables.put("keyAdvice", escapeHtml(detailedReview.getKeyAdvice()));
            } else {
                variables.put("keyAdvice", "핵심 조언이 없습니다");
            }
        } else {
            // Default values for detailed review
            variables.put("firstParagraphFullReview", "<strong>첫 문단</strong> 상세 리뷰가 없습니다");
            variables.put("firstParagraphQuote", "");
            variables.put("firstParagraphAnalysis", "상세 리뷰가 없습니다");
            variables.put("firstParagraphSuggestion", "");
            variables.put("middleSectionFullReview", "<strong>중간 부분</strong> 상세 리뷰가 없습니다");
            variables.put("middleSectionQuote", "");
            variables.put("middleSectionAnalysis", "상세 리뷰가 없습니다");
            variables.put("middleSectionSuggestion", "");
            variables.put("conclusionFullReview", "<strong>마무리</strong> 상세 리뷰가 없습니다");
            variables.put("conclusionQuote", "");
            variables.put("conclusionAnalysis", "상세 리뷰가 없습니다");
            variables.put("conclusionSuggestion", "");
            variables.put("keyAdvice", "상세 리뷰가 없습니다");
        }
        
        // Strengths and growth points
        if (event.getAnalysisResult() != null) {
            String strengths = formatListAsHtml(event.getAnalysisResult().getStrengths());
            String growthPoints = formatListAsHtml(event.getAnalysisResult().getGrowthPoints());
            
            variables.put("strengths", strengths.isEmpty() ? "<li>분석 중 강점을 찾지 못했습니다</li>" : strengths);
            variables.put("growthPoints", growthPoints.isEmpty() ? "<li>분석 중 개선점을 찾지 못했습니다</li>" : growthPoints);
        } else {
            variables.put("strengths", "<li>분석 결과가 없습니다</li>");
            variables.put("growthPoints", "<li>분석 결과가 없습니다</li>");
        }
        
        // Document length warning handling (from JsonNode for extra fields)
        try {
            JsonNode analysisResultNode = eventNode.get("analysisResult");
            if (analysisResultNode != null && analysisResultNode.has("documentLengthWarning")) {
                String warning = analysisResultNode.get("documentLengthWarning").asText();
                // Format as HTML for email template
                String warningHtml = "<div style=\"background-color:#fff3cd;padding:15px;border-radius:8px;margin:20px 0;border-left:4px solid #ffc107;\">" +
                                   "<p style=\"margin:0;color:#856404;font-size:14px;\">" + 
                                   "<strong>⚠️ 알림</strong><br/>" + 
                                   escapeHtml(warning) + 
                                   "</p></div>";
                variables.put("documentLengthWarning", warningHtml);
                log.info("[DOCUMENTO] Document length warning added for content: {}", event.getContentId());
            } else {
                // No warning, set empty string for template
                variables.put("documentLengthWarning", "");
            }
        } catch (Exception e) {
            log.warn("[DOCUMENTO] Failed to extract document length warning: {}", e.getMessage());
            variables.put("documentLengthWarning", "");
        }
        
        return variables;
    }
    
    private String findRatingValue(List<DocumentoParsedEvent.CategoryRating> ratings, String categoryName) {
        if (ratings == null || ratings.isEmpty()) {
            return "N/A";
        }
        
        for (DocumentoParsedEvent.CategoryRating rating : ratings) {
            if (rating != null && categoryName.equals(rating.getCategory())) {
                Integer ratingValue = rating.getRating();
                return ratingValue != null ? String.valueOf(ratingValue) : "N/A";
            }
        }
        return "N/A";
    }
    
    private String formatListAsHtml(java.util.List<String> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        
        return items.stream()
                .map(item -> "<li>" + escapeHtml(item) + "</li>")
                .collect(Collectors.joining("\n"));
    }
    
    private String generateStars(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            return "N/A";
        }
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                stars.append("⭐");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
    
    private String extractUserName(String email) {
        if (email == null || !email.contains("@")) {
            return "사용자";
        }
        // Extract username part before @
        String username = email.substring(0, email.indexOf("@"));
        // Capitalize first letter if it's lowercase
        if (!username.isEmpty()) {
            return username.substring(0, 1).toUpperCase() + username.substring(1);
        }
        return "사용자";
    }
    
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}