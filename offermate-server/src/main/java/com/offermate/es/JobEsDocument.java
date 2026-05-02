package com.offermate.es;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobEsDocument {

    private Long id;

    private Long companyId;

    private Long recruiterId;

    private String title;

    private Integer salaryMin;

    private Integer salaryMax;

    private String city;

    private String experience;

    private String education;

    private String tags;

    private String description;

    private Integer status;

    private Integer auditStatus;

    private Integer viewCount;

    private String companyName;

    private String companyLogo;

    private String industry;

    private String scale;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
