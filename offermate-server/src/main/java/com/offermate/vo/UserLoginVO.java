package com.offermate.vo;

import lombok.Data;

@Data
public class UserLoginVO {

    private String token;

    private Long userId;

    private String username;

    private Integer role;
}
