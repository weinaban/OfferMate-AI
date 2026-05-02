package com.offermate.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminJobAuditPageQueryDTO {

    @Size(max = 100, message = "关键词长度不能超过100")
    private String keyword;

    @Min(value = 0, message = "审核状态不合法")
    @Max(value = 2, message = "审核状态不合法")
    private Integer auditStatus;

    @Min(value = 1, message = "页码不能小于1")
    private Integer page;

    @Min(value = 1, message = "每页数量不能小于1")
    private Integer pageSize;
}
