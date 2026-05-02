package com.offermate.mq.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiLogMessage {

    private Long userId;

    private String module;

    private String prompt;

    private String result;

    private LocalDateTime createTime;
}
