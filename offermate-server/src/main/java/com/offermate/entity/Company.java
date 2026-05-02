package com.offermate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("company")
public class Company {

    @TableId(type = IdType.AUTO)
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
}
