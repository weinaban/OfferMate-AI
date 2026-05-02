package com.offermate.mq.consumer;

import com.offermate.constant.RabbitMQConstants;
import com.offermate.mapper.NotificationMapper;
import com.offermate.mq.dto.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InterviewNoticeConsumer extends AbstractNotificationConsumer {

    public InterviewNoticeConsumer(NotificationMapper notificationMapper) {
        super(notificationMapper);
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_INTERVIEW_NOTICE)
    public void consume(NotificationMessage message) {
        try {
            saveNotification(message);
        } catch (Exception e) {
            log.error("面试通知消费失败 userId={}", message == null ? null : message.getUserId(), e);
            throw e;
        }
    }
}
