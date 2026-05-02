package com.offermate.constant;

public class RabbitMQConstants {

    private RabbitMQConstants() {
    }

    public static final String OFFERMATE_EXCHANGE = "offermate.topic";

    public static final String ROUTING_DELIVERY_NOTICE = "notice.delivery";
    public static final String ROUTING_INTERVIEW_NOTICE = "notice.interview";
    public static final String ROUTING_COMPANY_AUDIT_NOTICE = "notice.company.audit";
    public static final String ROUTING_JOB_AUDIT_NOTICE = "notice.job.audit";
    public static final String ROUTING_AI_LOG = "ai.log";

    public static final String QUEUE_DELIVERY_NOTICE = "offermate.notice.delivery.queue";
    public static final String QUEUE_INTERVIEW_NOTICE = "offermate.notice.interview.queue";
    public static final String QUEUE_COMPANY_AUDIT_NOTICE = "offermate.notice.company.audit.queue";
    public static final String QUEUE_JOB_AUDIT_NOTICE = "offermate.notice.job.audit.queue";
    public static final String QUEUE_AI_LOG = "offermate.ai.log.queue";
}
