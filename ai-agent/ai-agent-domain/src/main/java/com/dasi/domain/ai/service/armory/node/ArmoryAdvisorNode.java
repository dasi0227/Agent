package com.dasi.domain.ai.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.ai.model.entity.ArmoryRequestEntity;
import com.dasi.domain.ai.model.enumeration.AiAdvisorType;
import com.dasi.domain.ai.model.vo.AiAdvisorVO;
import com.dasi.domain.ai.service.armory.ArmoryContext;
import com.dasi.domain.ai.service.armory.advisor.RagAnswerAdvisor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.dasi.domain.ai.model.enumeration.AiType.ADVISOR;

@Slf4j
@Service
public class ArmoryAdvisorNode extends AbstractArmoryNode {

    @Resource
    private VectorStore vectorStore;

    @Resource
    private ArmoryClientNode armoryClientNode;

    @Override
    protected String doApply(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) throws Exception {

        Set<AiAdvisorVO> aiAdvisorVOList = armoryContext.getValue(ADVISOR.getType());

        if (aiAdvisorVOList == null || aiAdvisorVOList.isEmpty()) {
            log.warn("【装配节点】ArmoryAdvisorNode：没有数据");
            return router(armoryRequestEntity, armoryContext);
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
            log.info("【装配节点】ArmoryAdvisorNode：advisorBeanName={}, advisorType={}", advisorBeanName, aiAdvisorVO.getAdvisorType());
        }

        return router(armoryRequestEntity, armoryContext);
    }

    @Override
    public StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> get(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) {
        return armoryClientNode;
    }


}
