package com.offermate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.offermate.dto.UserLoginDTO;
import com.offermate.dto.UserRegisterDTO;
import com.offermate.entity.SysUser;
import com.offermate.vo.CurrentUserVO;
import com.offermate.vo.UserLoginVO;

public interface SysUserService extends IService<SysUser> {

    void register(UserRegisterDTO registerDTO);

    UserLoginVO login(UserLoginDTO loginDTO);

    CurrentUserVO getCurrentUser();

    void updateAvatar(String avatar);

    void logout(String token);
}
