package com.offermate.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatSessionVO {

    private Long id;

    private Long sessionId;

    private Long seekerId;

    private Long recruiterId;

    private Long jobId;

    private String lastMessage;

    private LocalDateTime updateTime;

    private Integer unreadCount;

    private Long oppositeUserId;

    private String oppositeName;

    private String oppositeAvatar;

    private String companyName;

    private String companyLogo;

    private String seekerName;

    private String jobTitle;
}
