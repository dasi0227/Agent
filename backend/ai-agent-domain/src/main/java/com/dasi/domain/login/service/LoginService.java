package com.dasi.domain.login.service;

import com.dasi.domain.login.model.AuthContext;
import com.dasi.domain.login.model.command.ProfileUpdateCommand;
import com.dasi.domain.login.model.vo.LoginResponse;
import com.dasi.domain.login.model.vo.UserInfo;
import com.dasi.domain.login.repository.ILoginRepository;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LoginService implements ILoginService {

    @Resource
    private ILoginRepository loginRepository;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private JwtService jwtService;

    @Override
    public LoginResponse login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("用户名或密码不能为空");
        }
        User user = loginRepository.queryByUsername(username.trim());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return buildLoginResponse(user);
    }

    @Override
    public LoginResponse register(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("用户名或密码不能为空");
        }
        String normalizedUsername = username.trim();
        if (loginRepository.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = User.builder()
                .username(normalizedUsername)
                .password(passwordEncoder.encode(password))
                .role("account")
                .build();
        loginRepository.insertUser(user);
        return buildLoginResponse(user);
    }

    @Override
    public UserInfo current() {
        if (AuthContext.get() == null || AuthContext.getUserId() == null) {
            throw new IllegalArgumentException("未登录");
        }
        User user = loginRepository.queryById(AuthContext.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在或已被删除");
        }
        return toUserInfo(user);
    }

    @Override
    public LoginResponse updateProfile(ProfileUpdateCommand command) {
        if (AuthContext.getUserId() == null) {
            throw new IllegalArgumentException("未登录");
        }
        User user = loginRepository.queryById(AuthContext.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在或已被删除");
        }

        boolean needUpdate = false;

        if (StringUtils.hasText(command.getUsername()) && !command.getUsername().equals(user.getUsername())) {
            if (loginRepository.existsByUsernameExcludeId(command.getUsername(), user.getId())) {
                throw new IllegalArgumentException("用户名已存在");
            }
            user.setUsername(command.getUsername().trim());
            needUpdate = true;
        }

        if (StringUtils.hasText(command.getNewPassword())) {
            if (!StringUtils.hasText(command.getOldPassword())) {
                throw new IllegalArgumentException("请先输入旧密码");
            }
            if (!passwordEncoder.matches(command.getOldPassword(), user.getPassword())) {
                throw new IllegalArgumentException("旧密码不正确");
            }
            user.setPassword(passwordEncoder.encode(command.getNewPassword()));
            needUpdate = true;
        }

        if (needUpdate) {
            loginRepository.updateUser(user);
        }
        return buildLoginResponse(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        String token = jwtService.generateToken(user);
        return LoginResponse.builder()
                .token(token)
                .user(toUserInfo(user))
                .build();
    }

    private UserInfo toUserInfo(User user) {
        return UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
