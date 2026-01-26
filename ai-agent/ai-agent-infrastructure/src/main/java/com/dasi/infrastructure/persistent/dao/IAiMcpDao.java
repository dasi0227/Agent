package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiMcp;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IAiMcpDao {

    AiMcp queryByMcpId(String mcpId);

    List<AiMcp> queryChatMcpList();

    List<AiMcp> queryByMcpIdList(List<String> mcpIdList);

}
