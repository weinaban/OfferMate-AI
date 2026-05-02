package com.offermate.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageVO {

    private Long id;

    private Long sessionId;

    private Long senderId;

    private Long receiverId;

    private String content;

    private Integer msgType;

    private Integer isRead;

    private LocalDateTime createTime;
}
