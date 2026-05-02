package com.offermate.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewInvitationVO {

    private Long id;

    private Long seekerId;

    private Long recruiterId;

    private Long companyId;

    private Long jobId;

    private Long deliveryId;

    private LocalDateTime interviewTime;

    private String address;

    private String contactName;

    private String contactPhone;

    private String remark;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String jobTitle;

    private String companyName;

    private String companyLogo;

    private String seekerName;

    private String recruiterName;
}
