package com.dasi.domain.util.jwt;

import com.dasi.domain.auth.model.vo.UserVO;
import org.springframework.stereotype.Component;

@Component
public class AuthContext {

    private final ThreadLocal<UserVO> USER_CONTEXT = new ThreadLocal<>();

    public void set(UserVO userVO) {
        USER_CONTEXT.set(userVO);
    }

    public UserVO getUser() {
        return USER_CONTEXT.get();
    }

    public Long getId() {
        UserVO userVO = USER_CONTEXT.get();
        return userVO == null ? null : userVO.getId();
    }

    public void clear() {
        USER_CONTEXT.remove();
    }

}
