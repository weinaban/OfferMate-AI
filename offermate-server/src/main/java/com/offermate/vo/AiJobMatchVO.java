package com.offermate.vo;

import lombok.Data;

import java.util.List;

@Data
public class AiJobMatchVO {

    private Integer matchScore;

    private String matchLevel;

    private List<String> advantages;

    private List<String> weaknesses;

    private List<String> suggestions;

    private String summary;
}
