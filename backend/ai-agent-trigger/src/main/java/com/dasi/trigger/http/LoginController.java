package com.dasi.trigger.http;

import com.dasi.domain.login.model.command.ProfileUpdateCommand;
import com.dasi.domain.login.model.vo.LoginResponse;
import com.dasi.domain.login.model.vo.UserInfo;
import com.dasi.domain.login.service.ILoginService;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class LoginController {

    @Resource
    private ILoginService loginService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginService.login(request.getUsername(), request.getPassword());
        return Result.success(response);
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginService.register(request.getUsername(), request.getPassword());
        return Result.success(response);
    }

    @GetMapping("/me")
    public Result<UserInfo> current() {
        return Result.success(loginService.current());
    }

    @PutMapping("/me")
    public Result<LoginResponse> updateProfile(@RequestBody ProfileRequest request) {
        ProfileUpdateCommand command = ProfileUpdateCommand.builder()
                .username(request.getUsername())
                .oldPassword(request.getOldPassword())
                .newPassword(request.getNewPassword())
                .build();
        LoginResponse response = loginService.updateProfile(command);
        return Result.success(response);
    }

    @Data
    private static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    private static class ProfileRequest {
        private String username;
        private String oldPassword;
        private String newPassword;
    }
}
