package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiFlowDao {

    List<AiFlow> queryByAgentId(@Param("agentId") String agentId);

    void loadFlowPrompt(@Param("clientId") String clientId, @Param("flowPrompt") String flowPrompt);

    List<String> queryAgentIdByClientId(@Param("clientId") String clientId);

    AiFlow queryByAgentIdAndClientId(@Param("agentId") String agentId, @Param("clientId") String clientId);

    AiFlow queryById(@Param("id") Long id);

    void insert(AiFlow aiFlow);

    void update(AiFlow aiFlow);

    void delete(@Param("id") Long id);
}
