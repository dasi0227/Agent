package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IAiModelDao {
    AiModel queryByModelId(String modelId);

    List<AiModel> queryChatModelList();

}
