package com.offermate.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditDTO {

    @NotNull(message = "审核状态不能为空")
    @Min(value = 1, message = "审核状态不合法")
    @Max(value = 2, message = "审核状态不合法")
    private Integer auditStatus;
}
