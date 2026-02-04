package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IMessageDao {

    List<Message> queryBySessionAndType(@Param("sessionId") String sessionId, @Param("messageType") String messageType);

    Integer countBySessionAndRoleAndType(@Param("sessionId") String sessionId,
                                         @Param("messageRole") String messageRole,
                                         @Param("messageType") String messageType);

    Integer maxSeqBySessionAndType(@Param("sessionId") String sessionId, @Param("messageType") String messageType);

    void insert(Message message);

    void deleteBySessionId(@Param("sessionId") String sessionId);
}
