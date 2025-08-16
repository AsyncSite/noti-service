package com.asyncsite.notiservice.domain.model.event;

import com.asyncsite.notiservice.domain.model.Notification;

public record NotificationCreated (
    Notification notification
){

}
