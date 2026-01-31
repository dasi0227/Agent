package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiAgent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiAgentDao {

    AiAgent queryAgentByAgentId(String agentId);

    AiAgent queryAgentById(Long id);

    List<AiAgent> queryAgentList();

    List<AiAgent> queryAgentPage(@Param("keyword") String keyword,
                                 @Param("agentStatus") Integer agentStatus,
                                 @Param("agentType") String agentType,
                                 @Param("offset") Integer offset,
                                 @Param("size") Integer size);

    Long countAgent(@Param("keyword") String keyword,
                    @Param("agentStatus") Integer agentStatus,
                    @Param("agentType") String agentType);

    void insertAgent(AiAgent aiAgent);

    void updateAgent(AiAgent aiAgent);

    void deleteAgent(Long id);

}
