package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiTaskDao {

    List<AiTask> queryTaskList();

    int updateTaskStatus(@Param("taskId") String taskId, @Param("taskStatus") Integer taskStatus);

    List<AiTask> page(@Param("idKeyword") String idKeyword,
                      @Param("agentId") String agentId,
                      @Param("offset") Integer offset,
                      @Param("size") Integer size);

    Integer count(@Param("idKeyword") String idKeyword,
                  @Param("agentId") String agentId);

    AiTask queryById(@Param("id") Long id);

    AiTask queryByTaskId(@Param("taskId") String taskId);

    void insert(AiTask aiTask);

    void update(AiTask aiTask);

    void delete(@Param("id") Long id);

    void toggle(AiTask aiTask);

}
