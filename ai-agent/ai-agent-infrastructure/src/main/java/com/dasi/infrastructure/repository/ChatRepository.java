package com.dasi.infrastructure.repository;

import com.dasi.domain.chat.repository.IChatRepository;
import com.dasi.infrastructure.persistent.dao.IAiModelDao;
import com.dasi.infrastructure.persistent.po.AiModel;
import com.dasi.infrastructure.redis.IRedisService;
import com.dasi.types.dto.response.ModelResponse;
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
    public List<ModelResponse> queryModelResponseList() {
        List<ModelResponse> modelResponseList = redisService.getValue(MODEL_LIST_KEY);
        if (modelResponseList != null) {
            return modelResponseList;
        }

        List<AiModel> aiModelList = aiModelDao.queryModelList();
        if (aiModelList == null || aiModelList.isEmpty()) {
            modelResponseList = new ArrayList<>();
            redisService.setValue(MODEL_LIST_KEY, modelResponseList);
            return modelResponseList;
        }

        modelResponseList = aiModelList.stream()
                .map(aiModel -> ModelResponse.builder()
                        .modelId(aiModel.getModelId())
                        .modelName(aiModel.getModelName())
                        .build())
                .toList();

        redisService.setValue(MODEL_LIST_KEY, modelResponseList);
        return modelResponseList;
    }

}
