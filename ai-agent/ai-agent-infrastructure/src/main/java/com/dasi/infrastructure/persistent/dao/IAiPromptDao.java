package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiPrompt;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IAiPromptDao {

    AiPrompt queryByPromptId(String promptId);

    void updatePromptContent(String promptId, String promptContent);

}
