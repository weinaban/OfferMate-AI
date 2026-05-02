package com.offermate.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeliveryVO {

    private Long id;

    private Long userId;

    private Long jobId;

    private Long companyId;

    private Long resumeId;

    private Integer status;

    private LocalDateTime createTime;

    private String jobTitle;

    private String title;

    private String companyName;

    private String resumeTitle;

    private Long recruiterId;

    private String city;

    private Integer salaryMin;

    private Integer salaryMax;
}
