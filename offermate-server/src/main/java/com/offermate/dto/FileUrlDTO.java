package com.offermate.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FileUrlDTO {

    @Size(max = 500, message = "文件地址长度不能超过500")
    private String url;

    @Size(max = 500, message = "头像地址长度不能超过500")
    private String avatar;

    @Size(max = 500, message = "企业Logo地址长度不能超过500")
    private String logo;
}
