package com.offermate.common;

import lombok.Data;

@Data
public class Result<T> {

    public static final int SUCCESS_CODE = 1;
    public static final int ERROR_CODE = 0;
    public static final int UNAUTHORIZED_CODE = 401;
    public static final int FORBIDDEN_CODE = 403;
    public static final int SYSTEM_ERROR_CODE = 500;

    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = SUCCESS_CODE;
        result.msg = "success";
        result.data = data;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        return error(ERROR_CODE, msg);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.code = code;
        result.msg = msg;
        result.data = null;
        return result;
    }
}
