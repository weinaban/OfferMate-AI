package com.offermate.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_chat_memory")
public class AiChatMemory {

    @TableId
    private Long memoryId;

    private String messages;

    private LocalDateTime updateTime;
}
