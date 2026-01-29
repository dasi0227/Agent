package com.dasi.domain.ai.service.execute;

import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.dasi.domain.ai.repository.IAiRepository;
import com.dasi.domain.ai.model.entity.ExecuteResponseEntity;
import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
public abstract class AbstractExecuteNode extends AbstractMultiThreadStrategyRouter<ExecuteRequestEntity, ExecuteContext, String> {

    @Resource
    protected ApplicationContext applicationContext;

    @Resource
    protected IAiRepository aiRepository;

    public static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";

    public static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_retrieve_size";

    @Override
    protected void multiThread(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) {
        // 缺省的，可以让继承类不一定非要实现该方法
    }

    protected <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

    protected void sendSseMessage(ExecuteContext executeContext, ExecuteResponseEntity executeResponseEntity) {

        SseEmitter sseEmitter = executeContext.getValue("sseEmitter");
        if (sseEmitter == null) {
            return;
        }

        try {
            sseEmitter.send(SseEmitter.event()
                    .name("message")
                    .id(String.valueOf(executeResponseEntity.getTimestamp()))
                    .data(executeResponseEntity));
        } catch (Exception e) {
            log.error("【Agent 执行】发送 SSE 消息失败：{}", e.getMessage(), e);
        }

    }

    protected String extractJson(String content, String schema) {
        try {
            // 1) 去 think + 去代码块围栏
            String cleaned = content
                    .replaceAll("(?s)<think>.*?</think>", "")
                    .replace("<think>", "")
                    .replace("</think>", "")
                    .trim();

            // 2) 提取 JSON 块
            if ("{}".equals(schema)) {
                int start = cleaned.indexOf('{');
                int end = cleaned.lastIndexOf('}');
                cleaned = cleaned.substring(start, end + 1).trim();
            } else if ("[]".equals(schema)) {
                int start = cleaned.indexOf('[');
                int end = cleaned.lastIndexOf(']');
                cleaned = cleaned.substring(start, end + 1).trim();
            }

            // 3) 更改冒号
            cleaned = cleaned
                    .replaceAll("：\\s*", ":")
                    .replaceAll(":[ \\t]+", ": ")
                    .replaceAll(":(?![ \\t\\r\\n])(?!(//))", ": ")
                    .trim();

            // 4) 更改换行
            cleaned = cleaned.replace("\r\n", "\n").replace("\r", "\n");
            return cleaned;
        } catch (Exception e) {
            log.warn("【Agent 执行】JSON 提取失败：{}", e.getMessage(), e);
            throw e;
        }
    }

    protected JSONObject parseJsonObject(String json) {
        try {
            return JSON.parseObject(json);
        } catch (Exception e) {
            log.warn("【Agent 执行】JSON 解析失败：{}", e.getMessage(), e);
            throw e;
        }
    }

    protected JSONArray parseJsonArray(String json) {
        try {
            return JSON.parseArray(json);
        } catch (Exception e) {
            log.warn("【Agent 执行】JSON 解析失败：{}", e.getMessage(), e);
            throw e;
        }
    }

    protected JSONObject buildExceptionObject(String key, String message) {
        message = (message == null || message.isBlank()) ? "执行异常" : message;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(key, message);
        return jsonObject;
    }

    protected JSONArray buildExceptionArray(String key, String message) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(buildExceptionObject(key, message));
        return jsonArray;
    }

}
