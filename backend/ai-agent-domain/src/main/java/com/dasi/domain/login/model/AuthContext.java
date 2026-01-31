package com.dasi.domain.login.model;

public class AuthContext {

    private static final ThreadLocal<LoginUser> USER_HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(LoginUser loginUser) {
        USER_HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser loginUser = USER_HOLDER.get();
        return loginUser == null ? null : loginUser.getId();
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}
