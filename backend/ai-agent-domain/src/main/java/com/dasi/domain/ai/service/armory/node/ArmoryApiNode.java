package com.dasi.domain.ai.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.ai.model.entity.ArmoryRequestEntity;
import com.dasi.domain.ai.model.vo.AiApiVO;
import com.dasi.domain.ai.service.armory.ArmoryContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.dasi.domain.ai.model.enumeration.AiType.API;

@Slf4j
@Service
public class ArmoryApiNode extends AbstractArmoryNode {

    @Resource
    private ArmoryModelNode armoryModelNode;

    @Override
    protected String doApply(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) throws Exception {

        Set<AiApiVO> aiApiVOList = armoryContext.getValue(API.getType());

        if (aiApiVOList == null || aiApiVOList.isEmpty()) {
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
        return armoryModelNode;
    }

}
