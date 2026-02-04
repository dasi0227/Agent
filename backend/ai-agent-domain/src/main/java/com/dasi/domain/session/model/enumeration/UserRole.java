package com.dasi.domain.session.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum UserRole {

    ADMIN("管理员", "admin"),
    ACCOUNT("用户", "account")
    ;

    private String name;

    private String role;

}
