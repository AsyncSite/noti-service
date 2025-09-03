package com.asyncsite.notiservice.adapter.in.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Event DTO for documento parsing completion events received from parser-worker.
 * This class represents the deserialized form of the DocumentoParsedEvent from Kafka.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentoParsedEvent {
    
    @JsonProperty("eventId")
    private String eventId;
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("occurredAt")
    private Instant occurredAt;
    
    @JsonProperty("contentId")
    private String contentId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("userEmail")
    private String userEmail;
    
    @JsonProperty("isTrialUser")
    private boolean isTrialUser;
    
    @JsonProperty("url")
    private String url;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("summary")
    private String summary;
    
    @JsonProperty("analysisResult")
    private AnalysisResult analysisResult;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AnalysisResult {
        
        @JsonProperty("overallAssessment")
        private String overallAssessment;  // New field: Overall encouraging assessment (e.g., "전반적으로 잘 쓰셨어요! 👏")
        
        @JsonProperty("encouragementPhrase")
        private String encouragementPhrase;  // Dynamic encouragement phrase from Parser Worker
        
        @JsonProperty("categoryRatings")
        private List<CategoryRating> categoryRatings;  // Changed from Map to List to match Parser Worker output
        
        @JsonProperty("strengths")
        private List<String> strengths;
        
        @JsonProperty("growthPoints")
        private List<String> growthPoints;
        
        @JsonProperty("overallScore")
        private Double overallScore;
        
        @JsonProperty("keywords")
        private List<String> keywords;
        
        @JsonProperty("category")
        private String category;
        
        @JsonProperty("detailedReview")
        private DetailedReview detailedReview;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetailedReview {
        
        @JsonProperty("firstParagraph")
        private ReviewSection firstParagraph;
        
        @JsonProperty("middleSection")
        private ReviewSection middleSection;
        
        @JsonProperty("conclusion")
        private ReviewSection conclusion;
        
        @JsonProperty("keyAdvice")
        private String keyAdvice;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReviewSection {
        
        @JsonProperty("fullReview")
        private String fullReview;  // New field: Complete review paragraph (200-300 chars)
        
        @JsonProperty("quote")
        private String quote;  // Kept for backward compatibility
        
        @JsonProperty("analysis")
        private String analysis;  // Kept for backward compatibility
        
        @JsonProperty("suggestion")
        private String suggestion;  // Kept for backward compatibility
        
        @JsonProperty("highlight")
        private String highlight;  // New field: Section name to highlight (e.g., "첫 문단")
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryRating {
        
        @JsonProperty("category")
        private String category;  // Added category field (e.g., "제목 매력도")
        
        @JsonProperty("rating")
        private Integer rating;
        
        @JsonProperty("comment")
        private String comment;
    }
}