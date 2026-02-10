package com.dasi.domain.ai.service.task;

import com.dasi.domain.ai.model.vo.ScheduleVO;

import java.util.List;

public interface ITaskService {

    List<ScheduleVO> queryScheduleTaskList();

    void refreshTaskRegistry();

    void cancelTask(String taskId);

    void registerTask(ScheduleVO scheduleVO);

}
