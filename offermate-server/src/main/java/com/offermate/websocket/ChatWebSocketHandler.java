package com.offermate.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.offermate.dto.ChatSendDTO;
import com.offermate.exception.BusinessException;
import com.offermate.service.ChatMessageService;
import com.offermate.vo.ChatMessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Map<Long, WebSocketSession> ONLINE_SESSION_MAP = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final ChatMessageService chatMessageService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        ONLINE_SESSION_MAP.put(userId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            ONLINE_SESSION_MAP.remove(userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long senderId = (Long) session.getAttributes().get("userId");
        try {
            ChatSendDTO sendDTO = objectMapper.readValue(message.getPayload(), ChatSendDTO.class);
            ChatMessageVO messageVO = chatMessageService.sendMessage(senderId, sendDTO);
            sendChatMessage(session, messageVO);

            WebSocketSession receiverSession = ONLINE_SESSION_MAP.get(messageVO.getReceiverId());
            if (receiverSession != null && receiverSession.isOpen()) {
                sendChatMessage(receiverSession, messageVO);
            }
        } catch (BusinessException e) {
            sendErrorMessage(session, e.getMessage());
        } catch (Exception e) {
            sendErrorMessage(session, "消息发送失败");
        }
    }

    private void sendChatMessage(WebSocketSession session, ChatMessageVO messageVO) throws IOException {
        Map<String, Object> data = Map.of(
                "type", "chat",
                "data", messageVO
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(data)));
    }

    private void sendErrorMessage(WebSocketSession session, String msg) throws IOException {
        Map<String, Object> data = Map.of(
                "type", "error",
                "msg", msg
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(data)));
    }
}
