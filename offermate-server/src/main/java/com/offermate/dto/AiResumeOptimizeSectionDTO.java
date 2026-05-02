package com.offermate.dto;

import lombok.Data;

@Data
public class AiResumeOptimizeSectionDTO {

    private String section;

    private Long resumeId;

    private String title;

    private String realName;

    private String phone;

    private String email;

    private String education;

    private Integer experienceYear;

    private String skill;

    private String projectExp;

    private String selfIntro;

    private Long jobId;
}
