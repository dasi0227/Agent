package com.dasi.domain.agent.service.execute.node;

import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
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
                executeAutoResultEntity.setSectionContent(sanitizeSectionContent(executeAutoResultEntity.getSectionContent()));
                String sseData = "data: " + JSON.toJSONString(executeAutoResultEntity) + "\n\n";
                responseBodyEmitter.send(sseData);
            }
        } catch (Exception e) {
            log.error("【Agent 执行】error={}", e.getMessage(), e);
        }

    }

    protected String extractJson(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        String cleaned = content.replaceAll("(?s)<think>.*?</think>", "");
        cleaned = cleaned.replace("<think>", "").replace("</think>", "").trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1).trim();
        }
        return cleaned;
    }

    protected JSONObject parseJsonObject(String content) {
        String json = extractJson(content);
        if (json.isEmpty()) {
            return null;
        }
        try {
            return JSON.parseObject(json);
        } catch (Exception e) {
            log.warn("【Agent 执行】JSON 解析失败：{}", e.getMessage(), e);
            return null;
        }
    }

    protected String sanitizeSectionContent(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        String normalized = content.replace("\r\n", "\n").replace("\r", "\n");
        normalized = normalized.replace("\n", "\\n");
        return normalized.replace("\t", "\\t");
    }

}
