package com.offermate.config.langchain;

import com.offermate.entity.AiChatMemory;
import com.offermate.mapper.AiChatMemoryMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 把 LangChain4j 的 ChatMemory 持久化到 MySQL，进程重启后仍能恢复多轮对话历史。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PersistentChatMemoryStore implements ChatMemoryStore {

    private final AiChatMemoryMapper aiChatMemoryMapper;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        Long id = toLong(memoryId);
        if (id == null) {
            return new ArrayList<>();
        }
        AiChatMemory record = aiChatMemoryMapper.selectById(id);
        if (record == null || record.getMessages() == null || record.getMessages().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return ChatMessageDeserializer.messagesFromJson(record.getMessages());
        } catch (Exception e) {
            log.warn("反序列化对话历史失败 memoryId={}", id, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        Long id = toLong(memoryId);
        if (id == null) {
            return;
        }
        String json = ChatMessageSerializer.messagesToJson(messages);
        AiChatMemory existing = aiChatMemoryMapper.selectById(id);
        if (existing == null) {
            AiChatMemory record = new AiChatMemory();
            record.setMemoryId(id);
            record.setMessages(json);
            record.setUpdateTime(LocalDateTime.now());
            aiChatMemoryMapper.insert(record);
        } else {
            existing.setMessages(json);
            existing.setUpdateTime(LocalDateTime.now());
            aiChatMemoryMapper.updateById(existing);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        Long id = toLong(memoryId);
        if (id != null) {
            aiChatMemoryMapper.deleteById(id);
        }
    }

    private Long toLong(Object memoryId) {
        if (memoryId == null) {
            return null;
        }
        if (memoryId instanceof Long l) {
            return l;
        }
        if (memoryId instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(memoryId.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
