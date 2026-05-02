package com.offermate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("interview_invitation")
public class InterviewInvitation {

    @TableId(type = IdType.AUTO)
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
}
