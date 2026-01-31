package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiAdvisor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiAdvisorDao {

    AiAdvisor queryByAdvisorId(String advisorId);

    AiAdvisor queryById(Long id);

    List<AiAdvisor> queryPage(@Param("keyword") String keyword,
                              @Param("advisorType") String advisorType,
                              @Param("status") Integer status,
                              @Param("offset") Integer offset,
                              @Param("size") Integer size);

    Long count(@Param("keyword") String keyword,
               @Param("advisorType") String advisorType,
               @Param("status") Integer status);

    void insert(AiAdvisor aiAdvisor);

    void update(AiAdvisor aiAdvisor);

    void delete(Long id);

}
