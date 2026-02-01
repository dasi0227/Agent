package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiAgent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiAgentDao {

    AiAgent queryAgentByAgentId(String agentId);

    List<AiAgent> queryAgentList();

    List<AiAgent> page(@Param("idKeyword") String idKeyword,
                       @Param("nameKeyword") String nameKeyword,
                       @Param("agentType") String agentType,
                       @Param("offset") Integer offset,
                       @Param("size") Integer size);

    Integer count(@Param("idKeyword") String idKeyword,
                  @Param("nameKeyword") String nameKeyword,
                  @Param("agentType") String agentType);

    AiAgent query(@Param("id") Long id);

    void insert(AiAgent aiAgent);

    void update(AiAgent aiAgent);

    void delete(@Param("id") Long id);

    void toggle(AiAgent aiAgent);

}
