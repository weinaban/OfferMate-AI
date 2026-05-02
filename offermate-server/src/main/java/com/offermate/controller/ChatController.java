package com.offermate.controller;

import com.offermate.common.Result;
import com.offermate.dto.ChatSessionCreateDTO;
import com.offermate.service.ChatMessageService;
import com.offermate.service.ChatSessionService;
import com.offermate.vo.ChatMessageVO;
import com.offermate.vo.ChatSessionVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Validated
public class ChatController {

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;

    @PostMapping("/sessions")
    public Result<ChatSessionVO> createOrGetSession(@Valid @RequestBody ChatSessionCreateDTO dto) {
        return Result.success(chatSessionService.createOrGetSession(dto));
    }

    @GetMapping("/sessions")
    public Result<List<ChatSessionVO>> listMySessions() {
        return Result.success(chatSessionService.listMySessions());
    }

    @GetMapping("/sessions/{id}/messages")
    public Result<List<ChatMessageVO>> listMessages(@PathVariable @Min(value = 1, message = "会话ID不合法") Long id) {
        return Result.success(chatMessageService.listMessages(id));
    }

    @PutMapping("/sessions/{id}/read")
    public Result<Void> markRead(@PathVariable @Min(value = 1, message = "会话ID不合法") Long id) {
        chatMessageService.markRead(id);
        return Result.success();
    }
}
