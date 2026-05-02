package com.offermate.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobCreateDTO {

    private Long companyId;

    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 100, message = "岗位名称长度不能超过100")
    private String title;

    @Min(value = 0, message = "最低薪资不能小于0")
    private Integer salaryMin;

    @Min(value = 0, message = "最高薪资不能小于0")
    private Integer salaryMax;

    @Size(max = 50, message = "工作城市长度不能超过50")
    private String city;

    @Size(max = 50, message = "经验要求长度不能超过50")
    private String experience;

    @Size(max = 50, message = "学历要求长度不能超过50")
    private String education;

    @Size(max = 255, message = "技能标签长度不能超过255")
    private String tags;

    private String description;
}
