package com.dasi.domain.login.repository;

import com.dasi.domain.login.model.User;

public interface ILoginRepository {

    User queryByUsername(String username);

    User queryById(Long id);

    void insertUser(User user);

    void updateUser(User user);

    boolean existsByUsername(String username);

    boolean existsByUsernameExcludeId(String username, Long excludeId);
}
