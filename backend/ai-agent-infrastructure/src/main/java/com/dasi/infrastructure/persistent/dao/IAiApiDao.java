package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiApi;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiApiDao {

    AiApi queryByApiId(String apiId);

    List<AiApi> page(@Param("idKeyword") String idKeyword, @Param("offset") Integer offset, @Param("size") Integer size);

    Integer count(String idKeyword);

    AiApi query(String apiId);

    void insert(AiApi aiApi);

    void update(AiApi aiApi);

    void delete(String apiId);

    void toggle(AiApi aiApi);

}
