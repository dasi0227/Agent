package com.dasi.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryCommandEntity;
import com.dasi.domain.agent.model.vo.AiApiVO;
import com.dasi.domain.agent.service.armory.AbstractArmoryNode;
import com.dasi.domain.agent.service.armory.factory.ArmoryStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dasi.domain.agent.model.enumeration.AiEnum.API;

@Slf4j
@Service
public class AiApiNode extends AbstractArmoryNode {

    @Override
    protected String doApply(ArmoryCommandEntity armoryCommandEntity, ArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("【构建节点】AiApiNode");

        List<AiApiVO> aiApiVOList = dynamicContext.getValue(API.getCode());

        if (aiApiVOList == null || aiApiVOList.isEmpty()) {
            log.warn("【构建节点】AiApiNode：没有数据");
            return null;
        }

        for (AiApiVO aiApiVO : aiApiVOList) {
            OpenAiApi openAiApi = OpenAiApi.builder()
                    .baseUrl(aiApiVO.getApiBaseUrl())
                    .apiKey(aiApiVO.getApiKey())
                    .completionsPath(aiApiVO.getApiCompletionsPath())
                    .embeddingsPath(aiApiVO.getApiEmbeddingsPath())
                    .build();

            String beanName = API.getBeanName(aiApiVO.getApiId());
            registerBean(beanName, OpenAiApi.class, openAiApi);
            log.info("【构建节点】AiApiNode：beanName={}", beanName);
        }

        return router(armoryCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, ArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, ArmoryStrategyFactory.DynamicContext dynamicContext) {
        return defaultStrategyHandler;
    }

}
