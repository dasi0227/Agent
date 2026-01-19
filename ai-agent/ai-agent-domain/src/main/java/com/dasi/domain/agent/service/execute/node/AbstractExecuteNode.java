package com.dasi.domain.agent.service.execute.node;

import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import com.alibaba.fastjson2.JSON;
import com.dasi.domain.agent.adapter.IAgentRepository;
import com.dasi.domain.agent.model.entity.ExecuteAutoResultEntity;
import com.dasi.domain.agent.model.entity.ExecuteRequestEntity;
import com.dasi.domain.agent.service.execute.factory.ExecuteDynamicContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

@Slf4j
public abstract class AbstractExecuteNode extends AbstractMultiThreadStrategyRouter<ExecuteRequestEntity, ExecuteDynamicContext, String> {

    @Resource
    protected ApplicationContext applicationContext;

    @Resource
    protected IAgentRepository agentRepository;

    public static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";

    public static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_retrieve_size";

    @Override
    protected void multiThread(ExecuteRequestEntity executeRequestEntity, ExecuteDynamicContext executeDynamicContext) {
        // 缺省的，可以让继承类不一定非要实现该方法
    }

    protected <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

    protected void sendSseResult(ExecuteDynamicContext executeDynamicContext, ExecuteAutoResultEntity executeAutoResultEntity) {

        try {
            ResponseBodyEmitter responseBodyEmitter = executeDynamicContext.getValue("responseBodyEmitter");
            if (responseBodyEmitter != null) {
                String sseData = "data: " + JSON.toJSONString(executeAutoResultEntity) + "\n\n";
                responseBodyEmitter.send(sseData);
            }
        } catch (Exception e) {
            log.error("【Agent 执行】error={}", e.getMessage(), e);
        }

    }

}
