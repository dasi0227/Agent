package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiClient;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IAiClientDao {
    AiClient queryByClientId(String clientId);

    List<String> queryClientIdList();
}
