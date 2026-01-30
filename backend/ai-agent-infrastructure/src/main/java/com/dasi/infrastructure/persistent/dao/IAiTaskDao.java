package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IAiTaskDao {

    List<AiTask> queryTaskList();

    int updateTaskStatus(String taskId, Integer taskStatus);

}
