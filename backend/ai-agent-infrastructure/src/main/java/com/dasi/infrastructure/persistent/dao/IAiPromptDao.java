package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiPrompt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiPromptDao {

    AiPrompt queryByPromptId(@Param("promptId") String promptId);

    void loadPromptContent(String promptId, String promptContent);

    List<AiPrompt> page(@Param("idKeyword") String idKeyword,
                        @Param("nameKeyword") String nameKeyword,
                        @Param("offset") Integer offset,
                        @Param("size") Integer size);

    Integer count(@Param("idKeyword") String idKeyword,
                  @Param("nameKeyword") String nameKeyword);

    AiPrompt queryById(@Param("id") Long id);

    void insert(AiPrompt aiPrompt);

    void update(AiPrompt aiPrompt);

    void delete(@Param("id") Long id);

    void toggle(AiPrompt aiPrompt);

}
