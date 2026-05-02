package com.offermate.util;

import com.offermate.dto.LoginUserDTO;

public class UserContext {

    private static final ThreadLocal<LoginUserDTO> USER_THREAD_LOCAL = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUser(LoginUserDTO user) {
        USER_THREAD_LOCAL.set(user);
    }

    public static LoginUserDTO getUser() {
        return USER_THREAD_LOCAL.get();
    }

    public static void removeUser() {
        USER_THREAD_LOCAL.remove();
    }
}
