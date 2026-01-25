package com.dasi.infrastructure.repository;

import com.dasi.domain.chat.repository.IChatRepository;
import com.dasi.infrastructure.persistent.dao.IAiModelDao;
import com.dasi.infrastructure.redis.IRedisService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dasi.types.common.RedisConstant.MODEL_ID_LIST_KEY;

@Repository
public class ChatRepository implements IChatRepository {

    @Resource
    private IAiModelDao aiModelDao;

    @Resource
    private IRedisService redisService;

    @Override
    public List<String> queryModelIdList() {
        List<String> modelIdList = redisService.getValue(MODEL_ID_LIST_KEY, List.class);
        if (modelIdList != null && !modelIdList.isEmpty()) {
            return modelIdList;
        }

        modelIdList = aiModelDao.queryModelIdList();
        if (modelIdList != null) {
            redisService.setValue(MODEL_ID_LIST_KEY, modelIdList);
        }
        return modelIdList;
    }

}
