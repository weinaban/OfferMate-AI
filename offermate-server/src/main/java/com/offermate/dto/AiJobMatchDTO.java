package com.offermate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiJobMatchDTO {

    @NotNull(message = "简历ID不能为空")
    private Long resumeId;

    @NotNull(message = "岗位ID不能为空")
    private Long jobId;
}
