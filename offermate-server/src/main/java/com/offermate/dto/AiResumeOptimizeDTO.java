package com.offermate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiResumeOptimizeDTO {

    @NotNull(message = "简历ID不能为空")
    private Long resumeId;

    private Long jobId;
}
