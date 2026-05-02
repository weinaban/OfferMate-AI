package com.offermate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private Integer role;

    private String module;

    private String operation;

    private String method;

    private String requestUri;

    private String requestMethod;

    private String ip;

    private String params;

    private String result;

    private Integer status;

    private String errorMsg;

    private Long costTime;

    private LocalDateTime createTime;
}
