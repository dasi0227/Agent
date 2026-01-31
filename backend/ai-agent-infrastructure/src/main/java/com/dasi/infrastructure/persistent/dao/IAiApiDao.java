package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiApi;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiApiDao {

    AiApi queryByApiId(String apiId);

    AiApi queryById(Long id);

    List<AiApi> queryPage(@Param("keyword") String keyword,
                          @Param("status") Integer status,
                          @Param("offset") Integer offset,
                          @Param("size") Integer size);

    Long count(@Param("keyword") String keyword, @Param("status") Integer status);

    void insert(AiApi aiApi);

    void update(AiApi aiApi);

    void delete(Long id);
}
