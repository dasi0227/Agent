package com.dasi.domain.auth.service;

import com.dasi.types.dto.request.auth.AuthRequest;
import com.dasi.types.dto.request.auth.PasswordRequest;
import com.dasi.types.dto.response.auth.AuthResponse;

public interface IAuthService {

    AuthResponse login(AuthRequest request);

    AuthResponse register(AuthRequest request);

    AuthResponse profile();

    AuthResponse password(PasswordRequest request);

}
