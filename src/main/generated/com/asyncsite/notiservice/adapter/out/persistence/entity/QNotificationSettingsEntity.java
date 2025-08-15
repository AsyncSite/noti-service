package com.asyncsite.notiservice.adapter.out.persistence.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotificationSettingsEntity is a Querydsl query type for NotificationSettingsEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotificationSettingsEntity extends EntityPathBase<NotificationSettingsEntity> {

    private static final long serialVersionUID = 988198439L;

    public static final QNotificationSettingsEntity notificationSettingsEntity = new QNotificationSettingsEntity("notificationSettingsEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final BooleanPath discordEnabled = createBoolean("discordEnabled");

    public final BooleanPath emailEnabled = createBoolean("emailEnabled");

    public final BooleanPath marketing = createBoolean("marketing");

    public final BooleanPath pushEnabled = createBoolean("pushEnabled");

    public final BooleanPath studyUpdates = createBoolean("studyUpdates");

    public final StringPath timezone = createString("timezone");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final StringPath userId = createString("userId");

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QNotificationSettingsEntity(String variable) {
        super(NotificationSettingsEntity.class, forVariable(variable));
    }

    public QNotificationSettingsEntity(Path<? extends NotificationSettingsEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotificationSettingsEntity(PathMetadata metadata) {
        super(NotificationSettingsEntity.class, metadata);
    }

}

