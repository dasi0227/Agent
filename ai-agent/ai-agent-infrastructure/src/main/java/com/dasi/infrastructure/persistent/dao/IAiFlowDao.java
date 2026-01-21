package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiFlow;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IAiFlowDao {
    List<AiFlow> queryByAgentId(String aiAgentId);

    void loadFlowPrompt(String clientId, String flowPrompt);
}
