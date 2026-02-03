package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiAdvisor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiAdvisorDao {

    AiAdvisor queryByAdvisorId(@Param("advisorId") String advisorId);

    List<AiAdvisor> page(@Param("idKeyword") String idKeyword,
                         @Param("nameKeyword") String nameKeyword,
                         @Param("offset") Integer offset,
                         @Param("size") Integer size);

    Integer count(@Param("idKeyword") String idKeyword,
                  @Param("nameKeyword") String nameKeyword);

    AiAdvisor queryById(@Param("id") Long id);

    void insert(AiAdvisor aiAdvisor);

    void update(AiAdvisor aiAdvisor);

    void delete(@Param("id") Long id);

}
