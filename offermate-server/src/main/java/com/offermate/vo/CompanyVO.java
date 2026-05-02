package com.offermate.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompanyVO {

    private Long id;

    private Long userId;

    private String companyName;

    private String logo;

    private String industry;

    private String scale;

    private String address;

    private String intro;

    private Integer auditStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<JobPageVO> jobs;
}
