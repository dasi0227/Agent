package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.Session;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ISessionDao {

    List<Session> queryAll();

    List<Session> queryByUser(@Param("sessionUser") String sessionUser);

    Session queryById(@Param("id") Long id);

    Session queryBySessionId(@Param("sessionId") String sessionId);

    int countByUserAndType(@Param("sessionUser") String sessionUser, @Param("sessionType") String sessionType);

    void insert(Session session);

    void updateTitle(@Param("id") Long id, @Param("sessionTitle") String sessionTitle);

    void delete(@Param("id") Long id);

}
