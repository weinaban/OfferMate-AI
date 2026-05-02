package com.offermate.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryStatusDTO {

    @NotNull(message = "投递状态不能为空")
    @Min(value = 1, message = "投递状态不合法")
    @Max(value = 6, message = "投递状态不合法")
    private Integer status;
}
