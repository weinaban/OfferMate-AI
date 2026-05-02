package com.offermate.vo;

import lombok.Data;

@Data
public class AiInterviewQuestionVO {

    private Long sessionId;

    private String question;

    private Integer round;
}
