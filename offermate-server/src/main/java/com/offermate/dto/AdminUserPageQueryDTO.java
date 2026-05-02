package com.offermate.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUserPageQueryDTO {

    @Size(max = 100, message = "关键词长度不能超过100")
    private String keyword;

    @Min(value = 1, message = "角色不合法")
    @Max(value = 3, message = "角色不合法")
    private Integer role;

    @Min(value = 0, message = "用户状态不合法")
    @Max(value = 1, message = "用户状态不合法")
    private Integer status;

    @Min(value = 1, message = "页码不能小于1")
    private Integer page;

    @Min(value = 1, message = "每页数量不能小于1")
    private Integer pageSize;
}
