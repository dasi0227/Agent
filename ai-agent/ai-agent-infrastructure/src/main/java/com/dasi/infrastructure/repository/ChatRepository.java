package com.dasi.infrastructure.repository;

import com.dasi.domain.chat.repository.IChatRepository;
import com.dasi.infrastructure.persistent.dao.IAiClientDao;
import com.dasi.infrastructure.persistent.po.AiClient;
import com.dasi.infrastructure.redis.IRedisService;
import com.dasi.types.dto.response.ChatClientResponse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.dasi.types.common.RedisConstant.CLIENT_LIST_KEY;

@Repository
public class ChatRepository implements IChatRepository {

    @Resource
    private IAiClientDao aiClientDao;

    @Resource
    private IRedisService redisService;

    @Override
    public List<ChatClientResponse> queryChatClientResponseList() {

        List<ChatClientResponse> chatClientResponseList = redisService.getValue(CLIENT_LIST_KEY);
        if (chatClientResponseList != null) {
            return chatClientResponseList;
        }

        List<AiClient> aiClientList = aiClientDao.queryClientList();
        if (aiClientList == null || aiClientList.isEmpty()) {
            chatClientResponseList = new ArrayList<>();
            redisService.setValue(CLIENT_LIST_KEY, chatClientResponseList);
            return chatClientResponseList;
        }

        chatClientResponseList = aiClientList.stream()
                .map(aiClient -> ChatClientResponse.builder()
                        .clientId(aiClient.getClientId())
                        .clientName(aiClient.getClientName())
                        .build())
                .toList();

        redisService.setValue(CLIENT_LIST_KEY, chatClientResponseList);
        return chatClientResponseList;
    }

}
