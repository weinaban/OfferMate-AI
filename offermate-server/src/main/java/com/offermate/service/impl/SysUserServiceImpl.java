package com.offermate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offermate.constant.RedisConstants;
import com.offermate.dto.LoginUserDTO;
import com.offermate.dto.UserLoginDTO;
import com.offermate.dto.UserRegisterDTO;
import com.offermate.entity.SysUser;
import com.offermate.exception.BusinessException;
import com.offermate.mapper.SysUserMapper;
import com.offermate.service.RedisCacheService;
import com.offermate.service.SysUserService;
import com.offermate.util.JwtUtil;
import com.offermate.util.UserContext;
import com.offermate.vo.CurrentUserVO;
import com.offermate.vo.UserLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private static final int DEFAULT_ROLE = 1;
    private static final int NORMAL_STATUS = 1;
    private static final int DISABLED_STATUS = 0;

    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisCacheService redisCacheService;

    @Override
    public void register(UserRegisterDTO registerDTO) {
        if (registerDTO == null || !StringUtils.hasText(registerDTO.getUsername()) || !StringUtils.hasText(registerDTO.getPassword())) {
            throw new BusinessException("用户名和密码不能为空");
        }

        Integer role = registerDTO.getRole() == null ? DEFAULT_ROLE : registerDTO.getRole();
        if (role < 1 || role > 3) {
            throw new BusinessException("角色参数错误");
        }

        long count = count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, registerDTO.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setPhone(registerDTO.getPhone());
        user.setRole(role);
        user.setStatus(NORMAL_STATUS);
        save(user);
    }

    @Override
    public UserLoginVO login(UserLoginDTO loginDTO) {
        if (loginDTO == null || !StringUtils.hasText(loginDTO.getUsername()) || !StringUtils.hasText(loginDTO.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginDTO.getUsername()), false);
        if (user == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (DISABLED_STATUS == user.getStatus()) {
            throw new BusinessException("账号已被禁用");
        }

        String token = jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole());

        UserLoginVO loginVO = new UserLoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRole(user.getRole());
        return loginVO;
    }

    @Override
    public CurrentUserVO getCurrentUser() {
        LoginUserDTO loginUser = UserContext.getUser();
        if (loginUser == null) {
            throw new BusinessException("未登录");
        }

        SysUser user = getById(loginUser.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        CurrentUserVO currentUserVO = new CurrentUserVO();
        currentUserVO.setUserId(user.getId());
        currentUserVO.setUsername(user.getUsername());
        currentUserVO.setPhone(user.getPhone());
        currentUserVO.setAvatar(user.getAvatar());
        currentUserVO.setRole(user.getRole());
        currentUserVO.setStatus(user.getStatus());
        return currentUserVO;
    }

    @Override
    public void updateAvatar(String avatar) {
        if (!StringUtils.hasText(avatar)) {
            throw new BusinessException("头像地址不能为空");
        }
        LoginUserDTO loginUser = UserContext.getUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException("未登录");
        }

        SysUser user = getById(loginUser.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setAvatar(avatar.trim());
        updateById(user);
    }

    @Override
    public void logout(String authorization) {
        String token = getToken(authorization);
        if (StringUtils.hasText(token)) {
            try {
                long remainingMillis = jwtUtil.getRemainingMillis(token);
                if (remainingMillis > 0) {
                    redisCacheService.set(RedisConstants.TOKEN_BLACKLIST_KEY + token, "1", Duration.ofMillis(remainingMillis));
                }
            } catch (Exception ignored) {
                // token 已过期或格式错误时退出接口仍返回成功
            }
        }
        UserContext.removeUser();
    }

    private String getToken(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        String prefix = "Bearer ";
        if (authorization.startsWith(prefix)) {
            return authorization.substring(prefix.length());
        }
        return authorization;
    }
}
