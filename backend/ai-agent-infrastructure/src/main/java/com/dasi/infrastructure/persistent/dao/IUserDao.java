package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IUserDao {

    User queryById(@Param("id") Long id);

    User queryByUsername(@Param("username") String username);

    List<User> page(@Param("username") String username,
                    @Param("role") String role,
                    @Param("offset") Integer offset,
                    @Param("size") Integer size);

    Long count(@Param("username") String username,
               @Param("role") String role);

    void insert(User user);

    void update(User user);

    void delete(@Param("id") Long id);

}
