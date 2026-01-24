package com.dasi.domain.agent.service.dispatch;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.service.armory.ArmoryContext;
import com.dasi.domain.agent.service.armory.ArmoryStrategyFactory;
import com.dasi.domain.agent.service.armory.IArmoryStrategy;
import com.dasi.domain.agent.service.execute.ExecuteStrategyFactory;
import com.dasi.domain.agent.service.execute.IExecuteStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service
public class DispatchService implements IDispatchService {

    @Resource
    private ArmoryStrategyFactory armoryStrategyFactory;

    @Resource
    private ExecuteStrategyFactory executeStrategyFactory;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void dispatchArmoryStrategy(String armoryType, List<String> idList) {
        IArmoryStrategy armoryStrategy = armoryStrategyFactory.getArmoryStrategyByType(armoryType);
        StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> armoryRootNode = armoryStrategyFactory.getArmoryRootNode();

        if (armoryStrategy == null) {
            throw new IllegalStateException("装配策略不存在");
        }
        if (armoryRootNode == null) {
            throw new IllegalStateException("装配入口不存在");
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
            throw new RuntimeException("装配数据失败：" + armoryType);
        }
    }

    @Override
    public void dispatchExecuteStrategy(ExecuteRequestEntity executeRequestEntity, SseEmitter sseEmitter) {

        IExecuteStrategy executeStrategy = executeStrategyFactory.getStrategyByAgentId(executeRequestEntity.getAiAgentId());

        if (executeStrategy == null) {
            throw new IllegalStateException("执行策略不存在");
        }

        threadPoolExecutor.execute(() -> {
            try {
                log.info("【Agent 执行】开始");
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
                log.info("【Agent 执行】结束");
                sseEmitter.complete();
            }
        });
    }

}
