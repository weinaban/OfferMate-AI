package com.offermate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resume")
public class Resume {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String realName;

    private String phone;

    private String email;

    private String education;

    private Integer experienceYear;

    private String skill;

    private String projectExp;

    private String selfIntro;

    private Integer isDefault;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
