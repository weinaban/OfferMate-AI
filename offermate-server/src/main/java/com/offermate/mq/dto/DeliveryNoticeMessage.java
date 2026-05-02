package com.offermate.mq.dto;

import lombok.Data;

@Data
public class DeliveryNoticeMessage {

    private Long deliveryId;

    private Long seekerId;

    private Long recruiterId;

    private Long jobId;

    private String jobTitle;
}
