package com.offermate.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobPageVO {

    private Long id; // 职位ID

    private Long companyId; // 公司ID

    private String title; // 职位名称

    private Integer salaryMin; // 最低薪资

    private Integer salaryMax; // 最高薪资

    private String city; // 工作地点/城市

    private String experience; // 经验要求

    private String education; // 学历要求

    private String tags; // 职位标签

    private Integer status; // 职位状态（如：招聘中、已关闭等）

    private Integer auditStatus; // 审核状态（如：待审核、已通过、已拒绝等）

    private Integer viewCount; // 浏览次数

    private LocalDateTime createTime; // 创建时间

    private String companyName; // 公司名称

    private String companyLogo; // 公司Logo

    private String industry; // 所属行业

    private String scale; // 公司规模
}
