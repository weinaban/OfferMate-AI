package com.offermate.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminCompanyAuditVO {

    private Long id;

    private Long userId;

    private String username;

    private String companyName;

    private String logo;

    private String industry;

    private String scale;

    private String address;

    private String intro;

    private Integer auditStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
