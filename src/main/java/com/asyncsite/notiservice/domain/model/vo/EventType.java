package com.asyncsite.notiservice.domain.model.vo;

public enum EventType {
    // Study
    STUDY,
    STUDY_APPROVED,

    // Users
    PASSWORD_RESET,

    // Documento
    DOCUMENTO_ANALYZED,

    // QueryDaily
    QUERY_DAILY_QUESTION,

    // General
    NOTI,
    LOG,
    ACTION,
    USER_ACTION,
    SYSTEM
}
