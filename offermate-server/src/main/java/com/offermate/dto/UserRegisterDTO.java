package com.offermate.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 30, message = "密码长度必须在6到30之间")
    private String password;

    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    @Min(value = 1, message = "角色不合法")
    @Max(value = 3, message = "角色不合法")
    private Integer role;
}
