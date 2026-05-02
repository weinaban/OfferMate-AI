package com.offermate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FileDeleteDTO {

    @NotBlank(message = "文件地址不能为空")
    @Size(max = 500, message = "文件地址长度不能超过500")
    private String url;
}
