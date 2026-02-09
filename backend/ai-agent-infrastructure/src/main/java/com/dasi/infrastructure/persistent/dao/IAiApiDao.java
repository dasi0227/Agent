package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiApi;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiApiDao {

    AiApi queryByApiId(@Param("apiId") String apiId);

    List<AiApi> page(@Param("idKeyword") String idKeyword,
                     @Param("offset") Integer offset,
                     @Param("size") Integer size);

    Integer count(@Param("idKeyword") String idKeyword);

    Integer countAll();

    AiApi queryById(@Param("id") Long id);

    void insert(AiApi aiApi);

    void update(AiApi aiApi);

    void delete(@Param("id") Long id);

    List<String> listApiId();
}
