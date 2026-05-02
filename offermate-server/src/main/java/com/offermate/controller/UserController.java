package com.offermate.controller;

import com.offermate.common.Result;
import com.offermate.annotation.OperationLog;
import com.offermate.dto.UserLoginDTO;
import com.offermate.dto.UserRegisterDTO;
import com.offermate.service.SysUserService;
import com.offermate.vo.CurrentUserVO;
import com.offermate.vo.UserLoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        sysUserService.register(registerDTO);
        return Result.success();
    }

    @PostMapping("/login")
    @OperationLog(module = "用户认证", operation = "用户登录")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        return Result.success(sysUserService.login(loginDTO));
    }

    @GetMapping("/current")
    public Result<CurrentUserVO> current() {
        return Result.success(sysUserService.getCurrentUser());
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        sysUserService.logout(authorization);
        return Result.success();
    }
}
