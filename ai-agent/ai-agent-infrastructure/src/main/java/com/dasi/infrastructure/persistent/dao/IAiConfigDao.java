package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IAiConfigDao {

    List<AiConfig> queryByClientId(String clientId);

    List<String> queryClientIdListByConfigType(String configType);

}
