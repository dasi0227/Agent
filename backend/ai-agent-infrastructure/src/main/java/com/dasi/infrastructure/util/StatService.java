package com.dasi.infrastructure.util;

import com.dasi.domain.util.stat.IStatService;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.AiClient;
import com.dasi.infrastructure.persistent.po.AiConfig;
import com.dasi.infrastructure.persistent.po.AiFlow;
import com.dasi.infrastructure.persistent.po.AiModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.dasi.types.constant.StatConstant.*;

@Slf4j
@Service
public class StatService implements IStatService {

    @Resource
    private IAiStatDao aiStatDao;

    @Resource
    private IAiClientDao aiClientDao;

    @Resource
    private IAiModelDao aiModelDao;

    @Resource
    private IAiConfigDao aiConfigDao;

    @Resource
    private IAiFlowDao aiFlowDao;

    @Override
    public void recordChatUsage(String clientId, List<String> mcpIdList) {
        recordClientUsage(STAT_CHAT, clientId, mcpIdList);
    }

    @Override
    public void recordWorkUsage(String agentId) {
        increase(STAT_WORK, STAT_AGENT, agentId);
        aiFlowDao.queryByAgentId(agentId)
                .stream()
                .map(AiFlow::getClientId)
                .toList()
                .forEach(clientId -> recordClientUsage(STAT_WORK, clientId));
    }

    private void increase(String statCategory, String statKey, String statValue) {
        aiStatDao.upsert(LocalDate.now(), statCategory, statKey, statValue, 1);
    }

    private void recordClientUsage(String statCategory, String clientId) {
        recordClientUsage(statCategory, clientId, null);
    }

    private void recordClientUsage(String statCategory, String clientId, List<String> mcpIdList) {
        increase(statCategory, STAT_CLIENT, clientId);

        AiClient aiClient = aiClientDao.queryByClientId(clientId);
        String modelId = aiClient.getModelId();
        increase(statCategory, STAT_MODEL, modelId);

        AiModel aiModel = aiModelDao.queryByModelId(modelId);
        String apiId = aiModel.getApiId();
        increase(statCategory, STAT_API, apiId);

        for (AiConfig aiConfig : aiConfigDao.queryByClientIdAndConfigType(clientId, STAT_PROMPT)) {
            increase(statCategory, STAT_PROMPT, aiConfig.getConfigValue());
        }

        for (AiConfig aiConfig : aiConfigDao.queryByClientIdAndConfigType(clientId, STAT_ADVISOR)) {
            increase(statCategory, STAT_ADVISOR, aiConfig.getConfigValue());
        }

        for (AiConfig aiConfig : aiConfigDao.queryByClientIdAndConfigType(clientId, STAT_MCP)) {
            increase(statCategory, STAT_MCP, aiConfig.getConfigValue());
        }

        if (mcpIdList != null && !mcpIdList.isEmpty()) {
            for (String mcpId : mcpIdList) {
                increase(statCategory, STAT_MCP, mcpId);
            }
        }
    }
}
