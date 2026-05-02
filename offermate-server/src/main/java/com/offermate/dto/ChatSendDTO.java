package com.offermate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatSendDTO {

    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    @NotNull(message = "接收人ID不能为空")
    private Long receiverId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息内容不能超过1000字")
    private String content;

    @NotNull(message = "消息类型不能为空")
    private Integer msgType;
}
