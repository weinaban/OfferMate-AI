package com.offermate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.offermate.dto.ChatSendDTO;
import com.offermate.entity.ChatMessage;
import com.offermate.vo.ChatMessageVO;

import java.util.List;

public interface ChatMessageService extends IService<ChatMessage> {

    List<ChatMessageVO> listMessages(Long sessionId);

    void markRead(Long sessionId);

    ChatMessageVO sendMessage(Long senderId, ChatSendDTO dto);
}
