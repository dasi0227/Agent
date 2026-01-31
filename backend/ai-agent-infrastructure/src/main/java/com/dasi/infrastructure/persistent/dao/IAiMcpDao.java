package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiMcp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiMcpDao {

    AiMcp queryByMcpId(String mcpId);

    List<AiMcp> queryChatMcpList();

    List<AiMcp> queryByMcpIdList(List<String> mcpIdList);

    AiMcp queryById(Long id);

    List<AiMcp> queryPage(@Param("keyword") String keyword,
                          @Param("mcpType") String mcpType,
                          @Param("status") Integer status,
                          @Param("offset") Integer offset,
                          @Param("size") Integer size);

    Long count(@Param("keyword") String keyword,
               @Param("mcpType") String mcpType,
               @Param("status") Integer status);

    void insert(AiMcp aiMcp);

    void update(AiMcp aiMcp);

    void delete(Long id);

}
