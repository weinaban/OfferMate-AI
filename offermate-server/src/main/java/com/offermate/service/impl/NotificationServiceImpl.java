package com.offermate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.offermate.common.result.PageResult;
import com.offermate.dto.LoginUserDTO;
import com.offermate.dto.NotificationPageQueryDTO;
import com.offermate.entity.Notification;
import com.offermate.exception.BusinessException;
import com.offermate.mapper.NotificationMapper;
import com.offermate.service.NotificationService;
import com.offermate.util.PageUtils;
import com.offermate.util.UserContext;
import com.offermate.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public PageResult<NotificationVO> pageNotifications(NotificationPageQueryDTO dto) {
        LoginUserDTO loginUser = getLoginUser();
        if (dto == null) {
            dto = new NotificationPageQueryDTO();
        }
        int page = PageUtils.page(dto.getPage());
        int pageSize = PageUtils.pageSize(dto.getPageSize());

        Page<Notification> pageInfo = notificationMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, loginUser.getUserId())
                        .eq(dto.getType() != null, Notification::getType, dto.getType())
                        .eq(dto.getIsRead() != null, Notification::getIsRead, dto.getIsRead())
                        .orderByDesc(Notification::getCreateTime));

        List<NotificationVO> records = pageInfo.getRecords().stream()
                .map(this::toVO)
                .toList();
        return new PageResult<>(pageInfo.getTotal(), records);
    }

    @Override
    public Long getUnreadCount() {
        LoginUserDTO loginUser = getLoginUser();
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, loginUser.getUserId())
                .eq(Notification::getIsRead, 0));
    }

    @Override
    public void markRead(Long id) {
        if (id == null) {
            throw new BusinessException("通知不存在");
        }
        LoginUserDTO loginUser = getLoginUser();
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getUserId().equals(loginUser.getUserId())) {
            throw new BusinessException(403, "无权限");
        }
        if (Integer.valueOf(1).equals(notification.getIsRead())) {
            return;
        }
        notification.setIsRead(1);
        notificationMapper.updateById(notification);
    }

    @Override
    public void markAllRead() {
        LoginUserDTO loginUser = getLoginUser();
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, loginUser.getUserId())
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1));
    }

    private LoginUserDTO getLoginUser() {
        LoginUserDTO loginUser = UserContext.getUser();
        if (loginUser == null) {
            throw new BusinessException(401, "未登录");
        }
        return loginUser;
    }

    private NotificationVO toVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setUserId(notification.getUserId());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setType(notification.getType());
        vo.setIsRead(notification.getIsRead());
        vo.setCreateTime(notification.getCreateTime());
        return vo;
    }
}
