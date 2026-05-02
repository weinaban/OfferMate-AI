package com.offermate.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobPageVO {

    private Long id;

    private Long companyId;

    private String title;

    private Integer salaryMin;

    private Integer salaryMax;

    private String city;

    private String experience;

    private String education;

    private String tags;

    private Integer status;

    private Integer auditStatus;

    private Integer viewCount;

    private LocalDateTime createTime;

    private String companyName;

    private String companyLogo;

    private String industry;

    private String scale;
}
