package com.dasi.domain.auth.service;

import com.dasi.domain.auth.model.jwt.AuthContext;
import com.dasi.domain.auth.model.vo.UserVO;
import com.dasi.domain.auth.repository.IAuthRepository;
import com.dasi.domain.util.IJwtService;
import com.dasi.types.dto.request.auth.AuthRequest;
import com.dasi.types.dto.request.auth.PasswordRequest;
import com.dasi.types.dto.response.auth.AuthResponse;
import com.dasi.types.exception.AuthException;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService {

    @Resource
    private IAuthRepository authRepository;

    @Resource
    private IJwtService jwtService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private AuthContext authContext;

    @Override
    public AuthResponse login(AuthRequest request) {

        String username = request.getUsername();
        String password = request.getPassword();

        UserVO userVO = authRepository.queryByUsername(username);
        if (userVO == null) {
            throw new AuthException("用户不存在");
        }
        if (!passwordEncoder.matches(password, userVO.getPassword())) {
            throw new AuthException("用户名或密码错误");
        }
        assertUserActive(userVO);

        return buildAuthResponse(userVO);
    }

    @Override
    public AuthResponse register(AuthRequest request) {

        String username = request.getUsername();
        String password = request.getPassword();

        if (authRepository.queryByUsername(username) != null) {
            throw new AuthException("用户名已存在");
        }

        String encodedPwd = passwordEncoder.encode(password);
        UserVO userVO = authRepository.insertUser(username, encodedPwd);
        return buildAuthResponse(userVO);
    }

    @Override
    public AuthResponse profile() {
        if (authContext.getId() == null) {
            throw new AuthException("未登录");
        }

        UserVO userVO = authRepository.queryById(authContext.getId());
        if (userVO == null) {
            throw new AuthException("用户不存在或已被删除");
        }
        assertUserActive(userVO);

        return buildAuthResponse(userVO);
    }

    @Override
    public AuthResponse password(PasswordRequest request) {

        String username = request.getUsername();
        String newPassword = request.getNewPassword();
        String oldPassword = request.getOldPassword();

        UserVO userVO = authRepository.queryById(authContext.getId());
        if (userVO == null) {
            throw new AuthException("用户不存在或已被删除");
        }
        assertUserActive(userVO);

        String originalPassword = userVO.getPassword();
        if (!passwordEncoder.matches(oldPassword, originalPassword)) {
            throw new AuthException("旧密码不正确");
        }

        String encodedPwd = passwordEncoder.encode(newPassword);
        userVO = authRepository.updateUser(userVO.getId(), username, encodedPwd);
        return buildAuthResponse(userVO);
    }

    private AuthResponse buildAuthResponse(UserVO userVO) {
        String token = jwtService.generateToken(userVO);
        return AuthResponse.builder()
                .token(token)
                .id(userVO.getId())
                .username(userVO.getUsername())
                .role(userVO.getRole())
                .userStatus(userVO.getUserStatus())
                .build();
    }

    private void assertUserActive(UserVO userVO) {
        if (userVO != null && Integer.valueOf(0).equals(userVO.getUserStatus())) {
            throw new AuthException("账号已被禁用");
        }
    }

}
