package com.offermate.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        this(0, message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
