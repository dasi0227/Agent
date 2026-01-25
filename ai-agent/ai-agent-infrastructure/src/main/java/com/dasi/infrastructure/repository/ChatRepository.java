package com.dasi.infrastructure.repository;

import com.dasi.domain.chat.repository.IChatRepository;
import com.dasi.infrastructure.persistent.dao.IAiModelDao;
import com.dasi.infrastructure.persistent.po.AiModel;
import com.dasi.infrastructure.redis.IRedisService;
import com.dasi.types.dto.response.ChatModelResponse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.dasi.types.common.RedisConstant.MODEL_LIST_KEY;

@Repository
public class ChatRepository implements IChatRepository {

    @Resource
    private IAiModelDao aiModelDao;

    @Resource
    private IRedisService redisService;

    @Override
    public List<ChatModelResponse> queryChatModelResponseList() {
        List<ChatModelResponse> chatModelResponseList = redisService.getValue(MODEL_LIST_KEY);
        if (chatModelResponseList != null) {
            return chatModelResponseList;
        }

        List<AiModel> aiModelList = aiModelDao.queryChatModelList();
        if (aiModelList == null || aiModelList.isEmpty()) {
            chatModelResponseList = new ArrayList<>();
            redisService.setValue(MODEL_LIST_KEY, chatModelResponseList);
            return chatModelResponseList;
        }

        chatModelResponseList = aiModelList.stream()
                .map(aiModel -> ChatModelResponse.builder()
                        .modelId(aiModel.getModelId())
                        .modelName(aiModel.getModelName())
                        .build())
                .toList();

        redisService.setValue(MODEL_LIST_KEY, chatModelResponseList);
        return chatModelResponseList;
    }

}
