package com.asyncsite.notiservice.adapter.out.persistence.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotificationEntity is a Querydsl query type for NotificationEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotificationEntity extends EntityPathBase<NotificationEntity> {

    private static final long serialVersionUID = -544029404L;

    public static final QNotificationEntity notificationEntity = new QNotificationEntity("notificationEntity");

    public final EnumPath<com.asyncsite.notiservice.domain.model.vo.ChannelType> channelType = createEnum("channelType", com.asyncsite.notiservice.domain.model.vo.ChannelType.class);

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath notificationId = createString("notificationId");

    public final StringPath recipientContact = createString("recipientContact");

    public final NumberPath<Integer> retryCount = createNumber("retryCount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> sentAt = createDateTime("sentAt", java.time.LocalDateTime.class);

    public final EnumPath<com.asyncsite.notiservice.domain.model.vo.NotificationStatus> status = createEnum("status", com.asyncsite.notiservice.domain.model.vo.NotificationStatus.class);

    public final StringPath templateId = createString("templateId");

    public final StringPath title = createString("title");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final StringPath userId = createString("userId");

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QNotificationEntity(String variable) {
        super(NotificationEntity.class, forVariable(variable));
    }

    public QNotificationEntity(Path<? extends NotificationEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotificationEntity(PathMetadata metadata) {
        super(NotificationEntity.class, metadata);
    }

}

