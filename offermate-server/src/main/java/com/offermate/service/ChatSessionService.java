package com.offermate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.offermate.dto.ChatSessionCreateDTO;
import com.offermate.entity.ChatSession;
import com.offermate.vo.ChatSessionVO;

import java.util.List;

public interface ChatSessionService extends IService<ChatSession> {

    ChatSessionVO createOrGetSession(ChatSessionCreateDTO dto);

    List<ChatSessionVO> listMySessions();

    ChatSession checkSessionParticipant(Long sessionId, Long userId);
}
