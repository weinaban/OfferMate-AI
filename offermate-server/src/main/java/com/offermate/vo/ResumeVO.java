package com.offermate.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResumeVO {

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
