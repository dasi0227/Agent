package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiAdvisor;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IAiAdvisorDao {

    AiAdvisor queryByAdvisorId(String advisorId);

}
