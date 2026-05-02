package com.offermate.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiInterviewQuestionsDTO {

    @NotNull(message = "岗位ID不能为空")
    private Long jobId;

    @Min(value = 3, message = "面试题数量不能少于3")
    @Max(value = 10, message = "面试题数量不能超过10")
    private Integer count;
}
