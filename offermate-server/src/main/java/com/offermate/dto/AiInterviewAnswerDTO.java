package com.offermate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiInterviewAnswerDTO {

    @NotBlank(message = "回答内容不能为空")
    @Size(max = 3000, message = "回答内容不能超过3000字")
    private String answer;
}
