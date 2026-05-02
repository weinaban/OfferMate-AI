package com.offermate.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class NotificationPageQueryDTO {

    @Min(value = 1, message = "通知类型不合法")
    @Max(value = 5, message = "通知类型不合法")
    private Integer type;

    @Min(value = 0, message = "已读状态不合法")
    @Max(value = 1, message = "已读状态不合法")
    private Integer isRead;

    @Min(value = 1, message = "页码不能小于1")
    private Integer page;

    @Min(value = 1, message = "每页数量不能小于1")
    private Integer pageSize;
}
