package com.offermate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryCreateDTO {

    @NotNull(message = "岗位ID不能为空")
    private Long jobId;

    @NotNull(message = "简历ID不能为空")
    private Long resumeId;
}
