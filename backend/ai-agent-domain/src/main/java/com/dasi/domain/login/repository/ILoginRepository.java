package com.dasi.domain.login.repository;

public interface ILoginRepository {

    User queryByUsername(String username);

    User queryById(Long id);

    void insertUser(User user);

    void updateUser(User user);

    boolean existsByUsername(String username);

    boolean existsByUsernameExcludeId(String username, Long excludeId);
}
