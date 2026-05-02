package com.offermate.mq.consumer;

import com.offermate.constant.RabbitMQConstants;
import com.offermate.mapper.NotificationMapper;
import com.offermate.mq.dto.AuditNoticeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class JobAuditNoticeConsumer extends AbstractNotificationConsumer {

    private static final int TYPE_JOB_AUDIT = 4;

    public JobAuditNoticeConsumer(NotificationMapper notificationMapper) {
        super(notificationMapper);
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_JOB_AUDIT_NOTICE)
    public void consume(AuditNoticeMessage message) {
        try {
            String targetName = StringUtils.hasText(message.getTargetName()) ? message.getTargetName() : "未命名岗位";
            String content = message.getAuditStatus() == 1
                    ? "你发布的岗位【" + targetName + "】已审核通过"
                    : "你发布的岗位【" + targetName + "】审核未通过，请修改后重新提交";
            saveNotification(message.getUserId(), "岗位审核结果", content, TYPE_JOB_AUDIT);
        } catch (Exception e) {
            log.error("岗位审核通知消费失败 userId={}", message == null ? null : message.getUserId(), e);
            throw e;
        }
    }
}
