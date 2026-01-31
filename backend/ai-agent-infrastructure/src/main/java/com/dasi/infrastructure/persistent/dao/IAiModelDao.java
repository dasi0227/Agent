package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiModelDao {

    AiModel queryByModelId(String modelId);

    AiModel queryById(Long id);

    List<AiModel> queryPage(@Param("keyword") String keyword,
                            @Param("apiId") String apiId,
                            @Param("status") Integer status,
                            @Param("offset") Integer offset,
                            @Param("size") Integer size);

    Long count(@Param("keyword") String keyword,
               @Param("apiId") String apiId,
               @Param("status") Integer status);

    List<String> queryModelIdListByApiId(String apiId);

    void insert(AiModel aiModel);

    void update(AiModel aiModel);

    void delete(Long id);
}
