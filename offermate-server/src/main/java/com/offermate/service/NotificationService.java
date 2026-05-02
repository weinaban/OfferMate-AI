package com.offermate.service;

import com.offermate.common.result.PageResult;
import com.offermate.dto.NotificationPageQueryDTO;
import com.offermate.vo.NotificationVO;

public interface NotificationService {

    PageResult<NotificationVO> pageNotifications(NotificationPageQueryDTO dto);

    Long getUnreadCount();

    void markRead(Long id);

    void markAllRead();
}
