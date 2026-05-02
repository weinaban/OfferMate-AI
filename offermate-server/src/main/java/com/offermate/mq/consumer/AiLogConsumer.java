package com.offermate.mq.consumer;

import com.offermate.constant.RabbitMQConstants;
import com.offermate.entity.AiCallLog;
import com.offermate.mapper.AiCallLogMapper;
import com.offermate.mq.dto.AiLogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiLogConsumer {

    private final AiCallLogMapper aiCallLogMapper;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_AI_LOG)
    public void consume(AiLogMessage message) {
        try {
            AiCallLog logRecord = new AiCallLog();
            logRecord.setUserId(message.getUserId());
            logRecord.setModule(message.getModule());
            logRecord.setPrompt(message.getPrompt());
            logRecord.setResult(message.getResult());
            logRecord.setCreateTime(message.getCreateTime());
            aiCallLogMapper.insert(logRecord);
        } catch (Exception e) {
            log.error("AI调用日志消费失败 userId={}, module={}", message == null ? null : message.getUserId(),
                    message == null ? null : message.getModule(), e);
            throw e;
        }
    }
}
