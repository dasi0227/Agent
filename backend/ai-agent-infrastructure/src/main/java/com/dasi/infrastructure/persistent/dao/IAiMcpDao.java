package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiMcp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiMcpDao {

    AiMcp queryByMcpId(@Param("mcpId") String mcpId);

    List<AiMcp> queryChatMcpList();

    List<AiMcp> queryByMcpIdList(@Param("mcpIdList") List<String> mcpIdList);

    List<AiMcp> page(@Param("idKeyword") String idKeyword,
                     @Param("nameKeyword") String nameKeyword,
                     @Param("offset") Integer offset,
                     @Param("size") Integer size);

    Integer count(@Param("idKeyword") String idKeyword,
                  @Param("nameKeyword") String nameKeyword);

    Integer countAll();

    AiMcp queryById(@Param("id") Long id);

    void insert(AiMcp aiMcp);

    void update(AiMcp aiMcp);

    void delete(@Param("id") Long id);

}
