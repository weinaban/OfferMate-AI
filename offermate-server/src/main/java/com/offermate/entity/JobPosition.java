package com.offermate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("job_position")
public class JobPosition {

    @TableId(type = IdType.AUTO)
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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
