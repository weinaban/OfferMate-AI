package com.offermate.mq.consumer;

import com.offermate.constant.RabbitMQConstants;
import com.offermate.mapper.NotificationMapper;
import com.offermate.mq.dto.DeliveryNoticeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class DeliveryNoticeConsumer extends AbstractNotificationConsumer {

    private static final int TYPE_DELIVERY = 1;

    public DeliveryNoticeConsumer(NotificationMapper notificationMapper) {
        super(notificationMapper);
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_DELIVERY_NOTICE)
    public void consume(DeliveryNoticeMessage message) {
        try {
            String content = StringUtils.hasText(message.getJobTitle())
                    ? "你发布的【" + message.getJobTitle() + "】收到一份新的岗位投递"
                    : "你收到一份新的岗位投递";
            saveNotification(message.getRecruiterId(), "新的岗位投递", content, TYPE_DELIVERY);
        } catch (Exception e) {
            log.error("投递通知消费失败 deliveryId={}", message == null ? null : message.getDeliveryId(), e);
            throw e;
        }
    }
}
