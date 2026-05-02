package com.offermate.vo;

import lombok.Data;

import java.util.List;

@Data
public class AiInterviewReportVO {

    private Long sessionId;

    private Integer totalScore;

    private String level;

    private List<String> strengths;

    private List<String> weaknesses;

    private List<String> suggestions;

    private String report;
}
