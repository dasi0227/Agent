package com.dasi.domain.ai.service.dispatch;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.ai.model.entity.ArmoryRequestEntity;
import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import com.dasi.domain.ai.service.armory.ArmoryContext;
import com.dasi.domain.ai.service.armory.ArmoryStrategyFactory;
import com.dasi.domain.ai.service.armory.IArmoryStrategy;
import com.dasi.domain.ai.service.execute.ExecuteStrategyFactory;
import com.dasi.domain.ai.service.execute.IExecuteStrategy;
import com.dasi.domain.util.IRedisService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import static com.dasi.types.constant.RedisConstant.ARMORY_CACHE_PREFIX;

@Slf4j
@Service
public class DispatchService implements IDispatchService {

    @Resource
    private ArmoryStrategyFactory armoryStrategyFactory;

    @Resource
    private ExecuteStrategyFactory executeStrategyFactory;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private IRedisService redisService;

    @Override
    public void dispatchArmoryStrategy(String armoryType, Set<String> armoryIdSet) {

        IArmoryStrategy armoryStrategy = armoryStrategyFactory.getArmoryStrategyByType(armoryType);
        StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> armoryRootNode = armoryStrategyFactory.getArmoryRootNode();

        if (armoryStrategy == null) {
            throw new IllegalStateException("装配策略不存在");
        }
        if (armoryRootNode == null) {
            throw new IllegalStateException("装配入口不存在");
        }

        String armoryKey = ARMORY_CACHE_PREFIX + armoryType;
        Set<String> cacheSet = redisService.getSetMembers(armoryKey, String.class);

        if (cacheSet != null && !cacheSet.isEmpty()) {
            armoryIdSet.removeAll(cacheSet);
        }

        if (armoryIdSet.isEmpty()) {
            return;
        }

        ArmoryRequestEntity armoryRequestEntity = ArmoryRequestEntity.builder()
                .armoryType(armoryType)
                .armoryIdSet(armoryIdSet)
                .build();
        ArmoryContext armoryContext = new ArmoryContext();

        try {
            log.info("========================================================================================");
            armoryStrategy.armory(armoryRequestEntity, armoryContext);
            armoryRootNode.apply(armoryRequestEntity, armoryContext);
        } catch (Exception e) {
            log.error("【装配数据】error={}", e.getMessage(), e);
            throw new RuntimeException("装配数据失败：" + armoryType);
        }

        redisService.addSetMembers(armoryKey, armoryIdSet);
    }

    @Override
    public void dispatchExecuteStrategy(ExecuteRequestEntity executeRequestEntity, SseEmitter sseEmitter) {

        IExecuteStrategy executeStrategy = executeStrategyFactory.getStrategyByAgentId(executeRequestEntity.getAiAgentId());

        if (executeStrategy == null) {
            throw new IllegalStateException("执行策略不存在");
        }

        threadPoolExecutor.execute(() -> {
            try {
                log.info("========================================================================================");
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
                sseEmitter.complete();
            }
        });
    }

}
