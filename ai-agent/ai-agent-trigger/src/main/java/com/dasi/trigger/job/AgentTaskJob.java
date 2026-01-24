package com.dasi.trigger.job;

import com.dasi.domain.agent.service.task.ITaskService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AgentTaskJob {

    @Resource
    private ITaskService taskService;

    @Resource
    private TaskScheduler taskScheduler;

    @Scheduled(cron = "${schedule.refresh-cron}")
    public void refreshAgentTask() {
        taskService.refreshTaskRegistry();
    }

}
