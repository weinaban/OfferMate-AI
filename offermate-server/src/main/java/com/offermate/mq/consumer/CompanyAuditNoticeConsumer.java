package com.offermate.mq.consumer;

import com.offermate.constant.RabbitMQConstants;
import com.offermate.mapper.NotificationMapper;
import com.offermate.mq.dto.AuditNoticeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CompanyAuditNoticeConsumer extends AbstractNotificationConsumer {

    private static final int TYPE_COMPANY_AUDIT = 3;

    public CompanyAuditNoticeConsumer(NotificationMapper notificationMapper) {
        super(notificationMapper);
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_COMPANY_AUDIT_NOTICE)
    public void consume(AuditNoticeMessage message) {
        try {
            String content = message.getAuditStatus() == 1
                    ? "你的企业认证已通过"
                    : "你的企业认证未通过，请修改后重新提交";
            saveNotification(message.getUserId(), "企业审核结果", content, TYPE_COMPANY_AUDIT);
        } catch (Exception e) {
            log.error("企业审核通知消费失败 userId={}", message == null ? null : message.getUserId(), e);
            throw e;
        }
    }
}
