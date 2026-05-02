package com.offermate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_interview_session")
public class AiInterviewSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long jobId;

    private Long resumeId;

    private String title;

    private Integer status;

    private Integer totalScore;

    private String report;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
