package com.offermate.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogQueryDTO {

    private Long userId;

    @Size(max = 50, message = "用户名长度不能超过50")
    private String username;

    @Size(max = 50, message = "模块长度不能超过50")
    private String module;

    @Size(max = 100, message = "操作长度不能超过100")
    private String operation;

    @Min(value = 0, message = "日志状态不合法")
    @Max(value = 1, message = "日志状态不合法")
    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Min(value = 1, message = "页码不能小于1")
    private Integer page;

    @Min(value = 1, message = "每页数量不能小于1")
    private Integer pageSize;
}
