package com.offermate.vo;

import lombok.Data;

@Data
public class AiInterviewAnswerResultVO {

    private Long sessionId;

    private Integer score;

    private String comment;

    private String suggestion;

    private String followUpQuestion;

    private Boolean finished;
}
