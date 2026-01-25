package com.dasi.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
import com.dasi.domain.agent.model.vo.AiApiVO;
import com.dasi.domain.agent.service.armory.ArmoryContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dasi.domain.agent.model.enumeration.AiType.API;

@Slf4j
@Service
public class ArmoryApiNode extends AbstractArmoryNode {

    @Resource
    private ArmoryMcpNode armoryMcpNode;

    @Override
    protected String doApply(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) throws Exception {

        List<AiApiVO> aiApiVOList = armoryContext.getValue(API.getType());

        if (aiApiVOList == null || aiApiVOList.isEmpty()) {
            log.warn("【装配节点】ArmoryApiNode：没有数据");
            return router(armoryRequestEntity, armoryContext);
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
            log.info("【装配节点】ArmoryApiNode：apiBeanName={}, baseUrl={}", apiBeanName, aiApiVO.getApiBaseUrl());
        }

        return router(armoryRequestEntity, armoryContext);
    }

    @Override
    public StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> get(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) {
        return armoryMcpNode;
    }

}
