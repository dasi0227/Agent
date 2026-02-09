package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiModelDao {

    AiModel queryByModelId(@Param("modelId") String modelId);

    List<AiModel> page(@Param("idKeyword") String idKeyword,
                       @Param("nameKeyword") String nameKeyword,
                       @Param("apiId") String apiId,
                       @Param("offset") Integer offset,
                       @Param("size") Integer size);

    Integer count(@Param("idKeyword") String idKeyword,
                  @Param("nameKeyword") String nameKeyword);

    Integer countAll();

    AiModel queryById(@Param("id") Long id);

    void insert(AiModel aiModel);

    void update(AiModel aiModel);

    void delete(@Param("id") Long id);

    List<String> queryModelIdByApiId(@Param("apiId") String apiId);

    List<String> listModelId();
}
