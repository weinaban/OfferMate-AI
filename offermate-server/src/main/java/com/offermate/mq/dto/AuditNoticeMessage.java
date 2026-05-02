package com.offermate.mq.dto;

import lombok.Data;

@Data
public class AuditNoticeMessage {

    private Long userId;

    private String targetName;

    private Integer auditStatus;

    private String auditType;
}
