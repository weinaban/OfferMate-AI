package com.offermate.mq.consumer;

import com.offermate.entity.Notification;
import com.offermate.mapper.NotificationMapper;
import com.offermate.mq.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractNotificationConsumer {

    protected static final int UNREAD = 0;

    private final NotificationMapper notificationMapper;

    protected void saveNotification(Long userId, String title, String content, Integer type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(UNREAD);
        notificationMapper.insert(notification);
    }

    protected void saveNotification(NotificationMessage message) {
        saveNotification(message.getUserId(), message.getTitle(), message.getContent(), message.getType());
    }
}
