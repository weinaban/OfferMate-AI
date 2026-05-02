package com.offermate.controller;

import com.offermate.common.Result;
import com.offermate.dto.FileUrlDTO;
import com.offermate.exception.BusinessException;
import com.offermate.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seeker")
@RequiredArgsConstructor
public class SeekerController {

    private final SysUserService sysUserService;

    @PostMapping("/avatar")
    public Result<Void> updateAvatar(@Valid @RequestBody FileUrlDTO dto) {
        String avatar = dto == null ? null : dto.getAvatar();
        if (!StringUtils.hasText(avatar) && dto != null) {
            avatar = dto.getUrl();
        }
        if (!StringUtils.hasText(avatar)) {
            throw new BusinessException("头像地址不能为空");
        }
        sysUserService.updateAvatar(avatar);
        return Result.success();
    }
}
