package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiPrompt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiPromptDao {

    AiPrompt queryByPromptId(String promptId);

    void loadPromptContent(String promptId, String promptContent);

    AiPrompt queryById(Long id);

    List<String> queryClientIdListByPromptId(String promptId);

    List<AiPrompt> queryPage(@Param("keyword") String keyword,
                             @Param("status") Integer status,
                             @Param("offset") Integer offset,
                             @Param("size") Integer size);

    Long count(@Param("keyword") String keyword, @Param("status") Integer status);

    void insert(AiPrompt aiPrompt);

    void update(AiPrompt aiPrompt);

    void delete(Long id);

}
