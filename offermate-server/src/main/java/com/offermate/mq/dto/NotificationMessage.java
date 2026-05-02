package com.offermate.mq.dto;

import lombok.Data;

@Data
public class NotificationMessage {

    private Long userId;

    private String title;

    private String content;

    private Integer type;
}
