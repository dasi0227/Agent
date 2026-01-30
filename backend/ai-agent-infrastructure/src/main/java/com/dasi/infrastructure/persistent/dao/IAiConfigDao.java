package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiConfigDao {

    List<AiConfig> queryByClientIdAndConfigType(@Param("clientId") String clientId, @Param("configType") String configType);

    List<String> queryClientIdListByConfigType(String configType);

}
