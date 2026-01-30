package com.dasi.domain.ai.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.ai.model.entity.ArmoryRequestEntity;
import com.dasi.domain.ai.model.vo.AiModelVO;
import com.dasi.domain.ai.service.armory.ArmoryContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.dasi.domain.ai.model.enumeration.AiType.API;
import static com.dasi.domain.ai.model.enumeration.AiType.MODEL;

@Slf4j
@Service
public class ArmoryModelNode extends AbstractArmoryNode {

    @Resource
    private ArmoryMcpNode armoryMcpNode;

    @Override
    protected String doApply(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) throws Exception {

        Set<AiModelVO> aiModelVOList = armoryContext.getValue(MODEL.getType());

        if (aiModelVOList == null || aiModelVOList.isEmpty()) {
            log.warn("【装配节点】ArmoryModelNode：null");
            return router(armoryRequestEntity, armoryContext);
        }

        for (AiModelVO aiModelVO : aiModelVOList) {

            // 获取当前 Model 关联的 API
            String apiBeanName = API.getBeanName(aiModelVO.getApiId());
            OpenAiApi openAiApi = getBean(apiBeanName);
            if (openAiApi == null) {
                log.error("【装配节点】ArmoryModelNode：不存在 API");
            }

            // 实例化
            OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                    .model(aiModelVO.getModelName())
                    .build();
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(openAiChatOptions)
                    .build();

            // 注册 Bean 对象
            String modelBeanName = MODEL.getBeanName(aiModelVO.getModelId());
            registerBean(modelBeanName, OpenAiChatModel.class, chatModel);
            log.info("【装配节点】ArmoryModelNode：modelBeanName={}, modelType={}", modelBeanName, aiModelVO.getModelType());
        }

        return router(armoryRequestEntity, armoryContext);
    }

    @Override
    public StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> get(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) {
        return armoryMcpNode;
    }

}
