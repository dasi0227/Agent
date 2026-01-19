package com.dasi.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.agent.model.entity.ArmoryRequestEntity;
import com.dasi.domain.agent.model.enumeration.AiAdvisorType;
import com.dasi.domain.agent.model.vo.AiAdvisorVO;
import com.dasi.domain.agent.service.armory.advisor.RagAnswerAdvisor;
import com.dasi.domain.agent.service.armory.factory.ArmoryDynamicContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dasi.domain.agent.model.enumeration.AiType.ADVISOR;

@Slf4j
@Service
public class ArmoryAiAdvisorNode extends AbstractArmoryNode {

    @Resource
    private VectorStore vectorStore;

    @Resource
    private ArmoryAiClientNode armoryAiClientNode;

    @Override
    protected String doApply(ArmoryRequestEntity armoryRequestEntity, ArmoryDynamicContext armoryDynamicContext) throws Exception {

        List<AiAdvisorVO> aiAdvisorVOList = armoryDynamicContext.getValue(ADVISOR.getType());

        if (aiAdvisorVOList == null || aiAdvisorVOList.isEmpty()) {
            log.warn("【装配节点】ArmoryAiAdvisorNode：没有数据");
            return router(armoryRequestEntity, armoryDynamicContext);
        }

        for (AiAdvisorVO aiAdvisorVO : aiAdvisorVOList) {
            Advisor advisor = null;

            switch (AiAdvisorType.fromCode(aiAdvisorVO.getAdvisorType())) {
                case RAG_ANSWER -> {
                    AiAdvisorVO.RagAnswer ragAnswer = aiAdvisorVO.getRagAnswer();
                    SearchRequest searchRequest = SearchRequest.builder()
                            .topK(ragAnswer.getTopK())
                            .filterExpression(ragAnswer.getFilterExpression())
                            .build();
                    advisor = new RagAnswerAdvisor(vectorStore, searchRequest);
                }
                case CHAT_MEMORY -> {
                    AiAdvisorVO.ChatMemory chatMemory = aiAdvisorVO.getChatMemory();
                    MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder()
                            .maxMessages(chatMemory.getMaxMessages())
                            .build();
                    advisor = PromptChatMemoryAdvisor.builder(messageWindowChatMemory).build();
                }
            }

            String advisorBeanName = ADVISOR.getBeanName(aiAdvisorVO.getAdvisorId());
            registerBean(advisorBeanName, Advisor.class, advisor);
            log.info("【装配节点】ArmoryAiAdvisorNode：advisorBeanName={}, advisorType={}, advisorName={}", advisorBeanName, aiAdvisorVO.getAdvisorType(), aiAdvisorVO.getAdvisorName());
        }

        return router(armoryRequestEntity, armoryDynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryRequestEntity, ArmoryDynamicContext, String> get(ArmoryRequestEntity armoryRequestEntity, ArmoryDynamicContext armoryDynamicContext) {
        return armoryAiClientNode;
    }


}
