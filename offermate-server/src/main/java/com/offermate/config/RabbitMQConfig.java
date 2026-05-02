package com.offermate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.offermate.constant.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public TopicExchange offermateExchange() {
        return new TopicExchange(RabbitMQConstants.OFFERMATE_EXCHANGE, true, false);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }

    @Bean
    public Queue deliveryNoticeQueue() {
        return new Queue(RabbitMQConstants.QUEUE_DELIVERY_NOTICE, true);
    }

    @Bean
    public Queue interviewNoticeQueue() {
        return new Queue(RabbitMQConstants.QUEUE_INTERVIEW_NOTICE, true);
    }

    @Bean
    public Queue companyAuditNoticeQueue() {
        return new Queue(RabbitMQConstants.QUEUE_COMPANY_AUDIT_NOTICE, true);
    }

    @Bean
    public Queue jobAuditNoticeQueue() {
        return new Queue(RabbitMQConstants.QUEUE_JOB_AUDIT_NOTICE, true);
    }

    @Bean
    public Queue aiLogQueue() {
        return new Queue(RabbitMQConstants.QUEUE_AI_LOG, true);
    }

    @Bean
    public Binding deliveryNoticeBinding(Queue deliveryNoticeQueue, TopicExchange offermateExchange) {
        return BindingBuilder.bind(deliveryNoticeQueue).to(offermateExchange).with(RabbitMQConstants.ROUTING_DELIVERY_NOTICE);
    }

    @Bean
    public Binding interviewNoticeBinding(Queue interviewNoticeQueue, TopicExchange offermateExchange) {
        return BindingBuilder.bind(interviewNoticeQueue).to(offermateExchange).with(RabbitMQConstants.ROUTING_INTERVIEW_NOTICE);
    }

    @Bean
    public Binding companyAuditNoticeBinding(Queue companyAuditNoticeQueue, TopicExchange offermateExchange) {
        return BindingBuilder.bind(companyAuditNoticeQueue).to(offermateExchange).with(RabbitMQConstants.ROUTING_COMPANY_AUDIT_NOTICE);
    }

    @Bean
    public Binding jobAuditNoticeBinding(Queue jobAuditNoticeQueue, TopicExchange offermateExchange) {
        return BindingBuilder.bind(jobAuditNoticeQueue).to(offermateExchange).with(RabbitMQConstants.ROUTING_JOB_AUDIT_NOTICE);
    }

    @Bean
    public Binding aiLogBinding(Queue aiLogQueue, TopicExchange offermateExchange) {
        return BindingBuilder.bind(aiLogQueue).to(offermateExchange).with(RabbitMQConstants.ROUTING_AI_LOG);
    }
}
