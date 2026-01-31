package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiClient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiClientDao {
    AiClient queryByClientId(String clientId);

    List<AiClient> queryChatClientList();

    AiClient queryById(Long id);

    List<AiClient> queryPage(@Param("keyword") String keyword,
                             @Param("modelId") String modelId,
                             @Param("clientType") String clientType,
                             @Param("status") Integer status,
                             @Param("offset") Integer offset,
                             @Param("size") Integer size);

    Long count(@Param("keyword") String keyword,
               @Param("modelId") String modelId,
               @Param("clientType") String clientType,
               @Param("status") Integer status);

    void insert(AiClient aiClient);

    void update(AiClient aiClient);

    void delete(Long id);

    List<String> queryClientIdListByModelId(String modelId);

}
