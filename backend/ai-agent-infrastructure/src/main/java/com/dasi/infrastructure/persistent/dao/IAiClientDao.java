package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiClient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiClientDao {
    AiClient queryByClientId(@Param("clientId") String clientId);

    List<AiClient> queryChatClientList();

    List<AiClient> queryWorkClientList();

    List<AiClient> page(@Param("idKeyword") String idKeyword,
                        @Param("nameKeyword") String nameKeyword,
                        @Param("modelId") String modelId,
                        @Param("clientType") String clientType,
                        @Param("clientRole") String clientRole,
                        @Param("offset") Integer offset,
                        @Param("size") Integer size);

    Integer count(@Param("idKeyword") String idKeyword,
                  @Param("nameKeyword") String nameKeyword,
                  @Param("modelId") String modelId,
                  @Param("clientType") String clientType,
                  @Param("clientRole") String clientRole);

    AiClient queryById(@Param("id") Long id);

    void insert(AiClient aiClient);

    void update(AiClient aiClient);

    void delete(@Param("id") Long id);

    void toggle(AiClient aiClient);

    List<String> queryClientIdByModelId(@Param("modelId") String modelId);

}
