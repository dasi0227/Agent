package com.dasi.trigger.http;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.api.IAgentService;
import com.dasi.api.dto.ArmoryRequestDTO;
import com.dasi.api.dto.ExecuteRequestDTO;
import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.service.armory.ArmoryContext;
import com.dasi.domain.agent.service.armory.ArmoryStrategyFactory;
import com.dasi.domain.agent.service.armory.IArmoryStrategy;
import com.dasi.domain.agent.service.execute.ExecuteStrategyFactory;
import com.dasi.domain.agent.service.execute.IExecuteStrategy;
import com.dasi.types.model.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@RestController
@RequestMapping("/api/v1/agent")
public class AgentController implements IAgentService {

    @Resource
    private ArmoryStrategyFactory armoryStrategyFactory;

    @Resource
    private ExecuteStrategyFactory executeStrategyFactory;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    @PostMapping(value = "/armory")
    public Result<Void> armory(@Valid @RequestBody ArmoryRequestDTO armoryRequestDTO) {

        String armoryType = armoryRequestDTO.getArmoryType();
        List<String> idList = armoryRequestDTO.getIdList();

        IArmoryStrategy armoryStrategy = armoryStrategyFactory.getArmoryStrategyByType(armoryType);
        StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> armoryRootNode = armoryStrategyFactory.getArmoryRootNode();
        if (armoryStrategy == null || armoryRootNode == null) {
            return Result.error("装配策略不存在 || 装配入口不存在");
        }

        ArmoryRequestEntity armoryRequestEntity = ArmoryRequestEntity.builder()
                .armoryType(armoryType)
                .idList(idList)
                .build();
        ArmoryContext armoryContext = new ArmoryContext();

        try {
            armoryStrategy.armory(armoryRequestEntity, armoryContext);
            armoryRootNode.apply(armoryRequestEntity, armoryContext);
        } catch (Exception e) {
            log.error("【装配数据】error={}", e.getMessage(), e);
            return Result.error("装配数据失败：" + armoryType);
        }

        return Result.success();
    }

    @Override
    @PostMapping(value = "/execute", produces = "text/event-stream")
    public SseEmitter execute(@Valid @RequestBody ExecuteRequestDTO executeRequestDTO) {

        String executeType = executeRequestDTO.getExecuteType();

        // 1. 创建流式输出对象
        SseEmitter sseEmitter = new SseEmitter(0L);

        // 2. 构建执行命令实体
        ExecuteRequestEntity executeRequestEntity = ExecuteRequestEntity.builder()
                .aiAgentId(executeRequestDTO.getAiAgentId())
                .userMessage(executeRequestDTO.getUserMessage())
                .sessionId(executeRequestDTO.getSessionId())
                .maxRound(executeRequestDTO.getMaxRound())
                .build();

        // 3. 拿到具体的执行策略
        IExecuteStrategy executeStrategy = executeStrategyFactory.getStrategyByType(executeType);

        threadPoolExecutor.execute(() -> {
            try {
                log.info("【Agent 执行】类型={}", executeType);
                executeStrategy.execute(executeRequestEntity, sseEmitter);
            } catch (Exception e) {
                try {
                    log.error("【Agent 执行】error={}", e.getMessage(), e);
                    sseEmitter.send(SseEmitter.event()
                            .name("error")
                            .data("执行异常：" + e.getMessage()));
                } catch (Exception ex) {
                    log.error("【Agent 执行】error={}", e.getMessage(), e);
                }
            } finally {
                log.info("【Agent 执行】结束：type={}", executeType);
                sseEmitter.complete();
            }
        });

        return sseEmitter;
    }

}
