package com.dasi.domain.agent.service.execute.auto.node;

import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import com.dasi.domain.agent.adapter.IAgentRepository;
import com.dasi.domain.agent.model.entity.ExecuteCommandEntity;
import com.dasi.domain.agent.service.execute.auto.factory.AutoExecuteStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
public abstract class AbstractExecuteNode extends AbstractMultiThreadStrategyRouter<ExecuteCommandEntity, AutoExecuteStrategyFactory.DynamicContext, String> {

    @Resource
    protected ApplicationContext applicationContext;

    @Resource
    protected IAgentRepository agentRepository;

    public static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";

    public static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_retrieve_size";

    @Override
    protected void multiThread(ExecuteCommandEntity requestParameter, AutoExecuteStrategyFactory.DynamicContext dynamicContext) {
        // 缺省的，可以让继承类不一定非要实现该方法
    }

    protected <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

}
