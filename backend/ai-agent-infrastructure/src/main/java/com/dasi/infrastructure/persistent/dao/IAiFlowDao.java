package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiFlowDao {
    List<AiFlow> queryByAgentId(String aiAgentId);

    void loadFlowPrompt(String clientId, String flowPrompt);

    AiFlow queryById(Long id);

    List<AiFlow> queryPage(@Param("agentId") String agentId,
                           @Param("clientId") String clientId,
                           @Param("status") Integer status,
                           @Param("offset") Integer offset,
                           @Param("size") Integer size);

    Long count(@Param("agentId") String agentId,
               @Param("clientId") String clientId,
               @Param("status") Integer status);

    void insert(AiFlow aiFlow);

    void update(AiFlow aiFlow);

    void delete(Long id);

    List<AiFlow> queryByClientId(String clientId);

    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
