package com.offermate.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiInterviewSessionVO {

    private Long id;

    private Long jobId;

    private Long resumeId;

    private String title;

    private Integer status;

    private Integer totalScore;

    private String report;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
