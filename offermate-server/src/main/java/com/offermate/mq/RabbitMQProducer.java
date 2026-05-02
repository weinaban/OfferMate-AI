package com.offermate.mq;

import com.offermate.constant.RabbitMQConstants;
import com.offermate.mq.dto.AiLogMessage;
import com.offermate.mq.dto.AuditNoticeMessage;
import com.offermate.mq.dto.DeliveryNoticeMessage;
import com.offermate.mq.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendDeliveryNotice(DeliveryNoticeMessage message) {
        send(RabbitMQConstants.ROUTING_DELIVERY_NOTICE, message);
    }

    public void sendInterviewNotice(NotificationMessage message) {
        send(RabbitMQConstants.ROUTING_INTERVIEW_NOTICE, message);
    }

    public void sendCompanyAuditNotice(AuditNoticeMessage message) {
        send(RabbitMQConstants.ROUTING_COMPANY_AUDIT_NOTICE, message);
    }

    public void sendJobAuditNotice(AuditNoticeMessage message) {
        send(RabbitMQConstants.ROUTING_JOB_AUDIT_NOTICE, message);
    }

    public boolean sendAiLog(AiLogMessage message) {
        return send(RabbitMQConstants.ROUTING_AI_LOG, message);
    }

    private boolean send(String routingKey, Object message) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConstants.OFFERMATE_EXCHANGE, routingKey, message);
            return true;
        } catch (Exception e) {
            log.warn("RabbitMQ消息发送失败 routingKey={}", routingKey, e);
            return false;
        }
    }
}
