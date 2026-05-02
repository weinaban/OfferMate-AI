package com.offermate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offermate.constant.RedisConstants;
import com.offermate.dto.ChatSendDTO;
import com.offermate.dto.LoginUserDTO;
import com.offermate.entity.ChatMessage;
import com.offermate.entity.ChatSession;
import com.offermate.exception.BusinessException;
import com.offermate.mapper.ChatMessageMapper;
import com.offermate.service.ChatMessageService;
import com.offermate.service.ChatSessionService;
import com.offermate.service.RedisCacheService;
import com.offermate.util.UserContext;
import com.offermate.vo.ChatMessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    private static final int TEXT_MSG_TYPE = 1;
    private static final int UNREAD = 0;
    private static final int READ = 1;
    private static final int LAST_MESSAGE_MAX_LENGTH = 255;

    private final ChatSessionService chatSessionService;
    private final RedisCacheService redisCacheService;

    @Override
    public List<ChatMessageVO> listMessages(Long sessionId) {
        LoginUserDTO loginUser = getLoginUser();
        chatSessionService.checkSessionParticipant(sessionId, loginUser.getUserId());
        return list(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getCreateTime))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public void markRead(Long sessionId) {
        LoginUserDTO loginUser = getLoginUser();
        chatSessionService.checkSessionParticipant(sessionId, loginUser.getUserId());
        update(new LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getReceiverId, loginUser.getUserId())
                .set(ChatMessage::getIsRead, READ));
        redisCacheService.delete(RedisConstants.CHAT_UNREAD_KEY + loginUser.getUserId() + ":" + sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageVO sendMessage(Long senderId, ChatSendDTO dto) {
        if (dto == null || dto.getSessionId() == null) {
            throw new BusinessException("会话ID不能为空");
        }
        if (dto.getReceiverId() == null) {
            throw new BusinessException("接收人ID不能为空");
        }
        if (!StringUtils.hasText(dto.getContent())) {
            throw new BusinessException("消息内容不能为空");
        }
        if (dto.getContent().length() > 1000) {
            throw new BusinessException("消息内容不能超过1000字");
        }
        if (dto.getMsgType() != null && dto.getMsgType() != TEXT_MSG_TYPE) {
            throw new BusinessException("消息类型不支持");
        }

        ChatSession session = chatSessionService.checkSessionParticipant(dto.getSessionId(), senderId);
        boolean receiverInSession = session.getSeekerId().equals(dto.getReceiverId())
                || session.getRecruiterId().equals(dto.getReceiverId());
        if (!receiverInSession || senderId.equals(dto.getReceiverId())) {
            throw new BusinessException("无权限操作该会话");
        }

        ChatMessage message = new ChatMessage();
        message.setSessionId(dto.getSessionId());
        message.setSenderId(senderId);
        message.setReceiverId(dto.getReceiverId());
        message.setContent(dto.getContent().trim());
        message.setMsgType(TEXT_MSG_TYPE);
        message.setIsRead(UNREAD);
        save(message);
        redisCacheService.increment(RedisConstants.CHAT_UNREAD_KEY + dto.getReceiverId() + ":" + dto.getSessionId());
        redisCacheService.expire(RedisConstants.CHAT_UNREAD_KEY + dto.getReceiverId() + ":" + dto.getSessionId(),
                RedisConstants.CHAT_UNREAD_TTL);

        ChatSession updateSession = new ChatSession();
        updateSession.setId(session.getId());
        updateSession.setLastMessage(shortLastMessage(message.getContent()));
        updateSession.setUpdateTime(LocalDateTime.now());
        chatSessionService.updateById(updateSession);
        return toVO(message);
    }

    private LoginUserDTO getLoginUser() {
        LoginUserDTO loginUser = UserContext.getUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException("未登录");
        }
        return loginUser;
    }

    private String shortLastMessage(String content) {
        if (content.length() <= LAST_MESSAGE_MAX_LENGTH) {
            return content;
        }
        return content.substring(0, LAST_MESSAGE_MAX_LENGTH);
    }

    private ChatMessageVO toVO(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setSessionId(message.getSessionId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setContent(message.getContent());
        vo.setMsgType(message.getMsgType());
        vo.setIsRead(message.getIsRead());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }
}
