package com.offermate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanySaveDTO {

    @NotBlank(message = "企业名称不能为空")
    @Size(max = 100, message = "企业名称长度不能超过100")
    private String companyName;

    @Size(max = 255, message = "企业Logo长度不能超过255")
    private String logo;

    @Size(max = 50, message = "所属行业长度不能超过50")
    private String industry;

    @Size(max = 50, message = "公司规模长度不能超过50")
    private String scale;

    @Size(max = 255, message = "企业地址长度不能超过255")
    private String address;

    private String intro;
}
