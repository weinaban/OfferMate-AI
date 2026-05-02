package com.offermate.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobPageQueryDTO {

    @Size(max = 100, message = "关键词长度不能超过100")
    private String keyword;

    @Size(max = 50, message = "工作城市长度不能超过50")
    private String city;

    @Min(value = 0, message = "最低薪资不能小于0")
    private Integer salaryMin;

    @Min(value = 0, message = "最高薪资不能小于0")
    private Integer salaryMax;

    @Size(max = 50, message = "学历要求长度不能超过50")
    private String education;

    @Size(max = 50, message = "经验要求长度不能超过50")
    private String experience;

    @Min(value = 1, message = "页码不能小于1")
    private Integer page;

    @Min(value = 1, message = "每页数量不能小于1")
    private Integer pageSize;
}
