package com.asyncsite.notiservice.adapter.out.persistence.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotificationTemplateEntity is a Querydsl query type for NotificationTemplateEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotificationTemplateEntity extends EntityPathBase<NotificationTemplateEntity> {

    private static final long serialVersionUID = 1251010878L;

    public static final QNotificationTemplateEntity notificationTemplateEntity = new QNotificationTemplateEntity("notificationTemplateEntity");

    public final BooleanPath active = createBoolean("active");

    public final EnumPath<com.asyncsite.notiservice.domain.model.vo.ChannelType> channelType = createEnum("channelType", com.asyncsite.notiservice.domain.model.vo.ChannelType.class);

    public final StringPath contentTemplate = createString("contentTemplate");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final EnumPath<com.asyncsite.notiservice.domain.model.vo.EventType> eventType = createEnum("eventType", com.asyncsite.notiservice.domain.model.vo.EventType.class);

    public final StringPath templateId = createString("templateId");

    public final StringPath titleTemplate = createString("titleTemplate");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final StringPath variables = createString("variables");

    public final NumberPath<Integer> version = createNumber("version", Integer.class);

    public QNotificationTemplateEntity(String variable) {
        super(NotificationTemplateEntity.class, forVariable(variable));
    }

    public QNotificationTemplateEntity(Path<? extends NotificationTemplateEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotificationTemplateEntity(PathMetadata metadata) {
        super(NotificationTemplateEntity.class, metadata);
    }

}

