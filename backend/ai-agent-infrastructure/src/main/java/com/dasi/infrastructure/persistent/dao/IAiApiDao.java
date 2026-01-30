package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiApi;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IAiApiDao {

    AiApi queryByApiId(String apiId);

}
