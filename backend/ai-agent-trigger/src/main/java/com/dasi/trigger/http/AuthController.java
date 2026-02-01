package com.dasi.trigger.http;

import com.dasi.domain.auth.service.IAuthService;
import com.dasi.types.dto.request.auth.AuthRequest;
import com.dasi.types.dto.request.auth.PasswordRequest;
import com.dasi.types.dto.response.auth.AuthResponse;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Resource
    private IAuthService authService;

    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return Result.success(response);
    }

    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/profile")
    public Result<AuthResponse> profile() {
        return Result.success(authService.profile());
    }

    @PostMapping("/password")
    public Result<AuthResponse> password(@Valid @RequestBody PasswordRequest request) {
        return Result.success(authService.password(request));
    }

}
