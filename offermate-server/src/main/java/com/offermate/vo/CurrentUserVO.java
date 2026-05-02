package com.offermate.vo;

import lombok.Data;

@Data
public class CurrentUserVO {

    private Long userId;

    private String username;

    private String phone;

    private String avatar;

    private Integer role;

    private Integer status;
}
