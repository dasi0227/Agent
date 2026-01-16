package com.dasi.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryCommandEntity;
import com.dasi.domain.agent.model.vo.AiApiVO;
import com.dasi.domain.agent.service.armory.factory.ArmoryStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dasi.domain.agent.model.enumeration.AiType.API;

@Slf4j
@Service
public class ArmoryAiApiNode extends AbstractArmoryNode {

    @Resource
    private ArmoryAiMcpNode armoryAiMcpNode;

    @Override
    protected String doApply(ArmoryCommandEntity armoryCommandEntity, ArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {

        List<AiApiVO> aiApiVOList = dynamicContext.getValue(API.getCode());

        if (aiApiVOList == null || aiApiVOList.isEmpty()) {
            log.warn("【装配节点】ArmoryAiApiNode：没有数据");
            return router(armoryCommandEntity, dynamicContext);
        }

        for (AiApiVO aiApiVO : aiApiVOList) {
            // 实例化
            OpenAiApi openAiApi = OpenAiApi.builder()
                    .baseUrl(aiApiVO.getApiBaseUrl())
                    .apiKey(aiApiVO.getApiKey())
                    .completionsPath(aiApiVO.getApiCompletionsPath())
                    .embeddingsPath(aiApiVO.getApiEmbeddingsPath())
                    .build();

            // 注册 Bean 对象
            String apiBeanName = API.getBeanName(aiApiVO.getApiId());
            registerBean(apiBeanName, OpenAiApi.class, openAiApi);
            log.info("【装配节点】ArmoryAiApiNode：apiBeanName={}, baseUrl={}, apiKey={}", apiBeanName, aiApiVO.getApiBaseUrl(), aiApiVO.getApiKey());
        }

        return router(armoryCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, ArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, ArmoryStrategyFactory.DynamicContext dynamicContext) {
        return armoryAiMcpNode;
    }

}
