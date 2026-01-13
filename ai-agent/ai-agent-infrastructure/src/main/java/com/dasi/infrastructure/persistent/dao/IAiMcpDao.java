package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiMcp;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IAiMcpDao {
    AiMcp queryByMcpId(String mcpId);
}
