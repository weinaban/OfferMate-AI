package com.offermate.controller;

import com.offermate.common.Result;
import com.offermate.common.result.PageResult;
import com.offermate.dto.NotificationPageQueryDTO;
import com.offermate.service.NotificationService;
import com.offermate.vo.NotificationVO;
import com.offermate.vo.UnreadCountVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Result<PageResult<NotificationVO>> pageNotifications(@Valid @ModelAttribute NotificationPageQueryDTO dto) {
        return Result.success(notificationService.pageNotifications(dto));
    }

    @GetMapping("/unread/count")
    public Result<UnreadCountVO> getUnreadCount() {
        return Result.success(new UnreadCountVO(notificationService.getUnreadCount()));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable @Min(value = 1, message = "通知ID不合法") Long id) {
        notificationService.markRead(id);
        return Result.success();
    }

    @PutMapping("/read/all")
    public Result<Void> markAllRead() {
        notificationService.markAllRead();
        return Result.success();
    }
}
