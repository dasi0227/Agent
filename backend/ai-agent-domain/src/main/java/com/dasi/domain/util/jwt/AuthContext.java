package com.dasi.domain.util.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class AuthContext {

    private final ThreadLocal<UserInfo> USER_CONTEXT = new ThreadLocal<>();

    public void set(UserInfo userInfo) {
        USER_CONTEXT.set(userInfo);
    }

    public UserInfo getUser() {
        return USER_CONTEXT.get();
    }

    public String getUsername() {
        UserInfo userInfo = USER_CONTEXT.get();
        return userInfo == null ? null : userInfo.getUsername();
    }

    public Long getId() {
        UserInfo userInfo = USER_CONTEXT.get();
        return userInfo == null ? null : userInfo.getId();
    }

    public String getRole() {
        UserInfo userInfo = USER_CONTEXT.get();
        return userInfo == null ? null : userInfo.getRole();
    }

    public void clear() {
        USER_CONTEXT.remove();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String role;
    }
}
