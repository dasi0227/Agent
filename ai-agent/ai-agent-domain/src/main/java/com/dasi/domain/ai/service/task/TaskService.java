package com.dasi.domain.ai.service.task;

import com.dasi.domain.ai.repository.IAiRepository;
import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import com.dasi.domain.ai.model.vo.AiTaskVO;
import com.dasi.domain.ai.model.vo.ScheduleVO;
import com.dasi.domain.ai.service.dispatch.IDispatchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
public class TaskService implements ITaskService {

    @Resource
    private IAiRepository aiRepository;

    @Resource
    private IDispatchService dispatchService;

    @Resource
    private TaskScheduler taskScheduler;

    private final Map<String, ScheduledFuture<?>> scheduleMap = new ConcurrentHashMap<>();

    private final Map<String, String> signatureMap = new ConcurrentHashMap<>();

    @Override
    public List<ScheduleVO> queryScheduleTaskList() {

        List<AiTaskVO> aiTaskVOList = aiRepository.queryTaskVOList();

        List<ScheduleVO> scheduleVOList = new ArrayList<>();
        for (AiTaskVO aiTaskVO : aiTaskVOList) {
            ScheduleVO scheduleVO = new ScheduleVO();
            scheduleVO.setScheduleId(aiTaskVO.getTaskId());
            scheduleVO.setScheduleCron(new CronTrigger(aiTaskVO.getTaskCron()));
            scheduleVO.setScheduleSignature(aiTaskVO.buildSignature());
            scheduleVO.setScheduleExecutor(() -> {
                try {
                    ExecuteRequestEntity executeRequestEntity = ExecuteRequestEntity.builder()
                            .aiAgentId(aiTaskVO.getAgentId())
                            .sessionId("s-task-" + System.currentTimeMillis())
                            .maxRetry(aiTaskVO.getTaskParam().getMaxRetry())
                            .maxRound(aiTaskVO.getTaskParam().getMaxRound())
                            .userMessage(aiTaskVO.getTaskParam().getUserMessage())
                            .build();
                    SseEmitter sseEmitter = new SseEmitter(0L);
                    dispatchService.dispatchExecuteStrategy(executeRequestEntity, sseEmitter);
                } catch (Exception e) {
                    log.error("【定时任务】查询失败：error={}", e.getMessage(), e);
                }
            });
            scheduleVOList.add(scheduleVO);
        }

        return scheduleVOList;
    }

    @Override
    public synchronized void refreshTaskRegistry() {

        // 1. 拿到最新配置的任务列表
        Map<String, ScheduleVO> latestScheduleMap = new ConcurrentHashMap<>();
        List<ScheduleVO> scheduleVOList = queryScheduleTaskList();
        for (ScheduleVO scheduleVO : scheduleVOList) {
            latestScheduleMap.put(scheduleVO.getScheduleId(), scheduleVO);
        }

        // 2. 之前配置的任务 id 不在最新的任务列表则删除
        for (String scheduleId : scheduleMap.keySet()) {
            if (!latestScheduleMap.containsKey(scheduleId)) {
                cancelTask(scheduleId);
            }
        }

        // 3. 更新任务配置
        for (ScheduleVO scheduleVO : scheduleVOList) {
            String scheduleId = scheduleVO.getScheduleId();

            String newSignature = scheduleVO.getScheduleSignature();
            String oldSignature = signatureMap.get(scheduleId);

            if (oldSignature == null) {
                registerTask(scheduleVO);
                continue;
            }

            if (!oldSignature.equals(newSignature)) {
                cancelTask(scheduleId);
                registerTask(scheduleVO);
            }
        }
    }

    @Override
    public boolean registerTask(ScheduleVO scheduleVO) {
        try {
            ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(
                    scheduleVO.getScheduleExecutor(),
                    scheduleVO.getScheduleCron()
            );

            if (scheduledFuture == null) {
                return false;
            }

            scheduleMap.put(scheduleVO.getScheduleId(), scheduledFuture);
            signatureMap.put(scheduleVO.getScheduleId(), scheduleVO.getScheduleSignature());
            return true;
        } catch (Exception e) {
            log.error("【定时任务】注册失败：error={}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean cancelTask(String scheduleId) {
        try {
            ScheduledFuture<?> future = scheduleMap.remove(scheduleId);
            signatureMap.remove(scheduleId);
            if (future == null) {
                return false;
            }
            return future.cancel(false);
        } catch (Exception e) {
            log.error("【定时任务】取消失败：error={}", e.getMessage(), e);
            return false;
        }
    }

}
