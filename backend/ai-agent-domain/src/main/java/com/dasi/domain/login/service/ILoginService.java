package com.dasi.domain.login.service;

import com.dasi.domain.login.model.command.ProfileUpdateCommand;
import com.dasi.domain.login.model.vo.LoginResponse;
import com.dasi.domain.login.model.vo.UserInfo;

public interface ILoginService {

    LoginResponse login(String username, String password);

    LoginResponse register(String username, String password);

    UserInfo current();

    LoginResponse updateProfile(ProfileUpdateCommand command);
}
