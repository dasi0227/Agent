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

import static com.dasi.domain.agent.model.enumeration.AiEnum.API;

@Slf4j
@Service
public class AiApiNode extends AbstractArmoryNode {

    @Resource
    private AiMcpNode aiMcpNode;

    @Override
    protected String doApply(ArmoryCommandEntity armoryCommandEntity, ArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {

        List<AiApiVO> aiApiVOList = dynamicContext.getValue(API.getCode());

        if (aiApiVOList == null || aiApiVOList.isEmpty()) {
            log.warn("【构建节点】AiApiNode：没有数据");
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
            log.info("【构建节点】AiApiNode：apiBeanName={}", apiBeanName);
        }

        return router(armoryCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, ArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, ArmoryStrategyFactory.DynamicContext dynamicContext) {
        return aiMcpNode;
    }

}
