package com.offermate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResumeUpdateDTO {

    @NotBlank(message = "简历名称不能为空")
    @Size(max = 100, message = "简历名称长度不能超过100")
    private String title;

    @Size(max = 50, message = "姓名长度不能超过50")
    private String realName;

    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    @Size(max = 50, message = "学历长度不能超过50")
    private String education;

    @Min(value = 0, message = "工作年限不能小于0")
    private Integer experienceYear;

    private String skill;

    private String projectExp;

    private String selfIntro;
}
