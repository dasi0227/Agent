package com.dasi.api;

import com.dasi.types.dto.request.auth.AuthRequest;
import com.dasi.types.dto.request.auth.PasswordRequest;
import com.dasi.types.dto.response.auth.AuthResponse;
import com.dasi.types.dto.result.Result;

public interface IAuthApi {

    Result<AuthResponse> login(AuthRequest request);

    Result<AuthResponse> register(AuthRequest request);

    Result<AuthResponse> profile();

    Result<AuthResponse> password(PasswordRequest request);
}

