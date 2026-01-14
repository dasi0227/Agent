package com.dasi.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryCommandEntity;
import com.dasi.domain.agent.model.enumeration.AiType;
import com.dasi.domain.agent.service.armory.factory.ArmoryStrategyFactory;
import com.dasi.domain.agent.service.armory.load.ILoadStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class RootNode extends AbstractArmoryNode {

    private final Map<String, ILoadStrategy> loadStrategyMap;

    @Resource
    private AiApiNode aiApiNode;

    public RootNode(Map<String, ILoadStrategy> loadStrategyMap) {
        this.loadStrategyMap = loadStrategyMap;
    }

    @Override
    protected void multiThread(ArmoryCommandEntity armoryCommandEntity, ArmoryStrategyFactory.DynamicContext dynamicContext) {
        String commandType = armoryCommandEntity.getCommandType();
        String loadStrategyKey = AiType.getLoadStrategyByCode(commandType);
        ILoadStrategy loadStrategy = loadStrategyMap.get(loadStrategyKey);
        log.info("【加载数据】type={}", commandType);
        loadStrategy.loadData(armoryCommandEntity, dynamicContext);
    }

    @Override
    protected String doApply(ArmoryCommandEntity armoryCommandEntity, ArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("【构建节点】RootNode");
        return router(armoryCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, ArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, ArmoryStrategyFactory.DynamicContext dynamicContext) {
        return aiApiNode;
    }

}
