package com.dasi.http;

import com.dasi.IQueryService;
import com.dasi.result.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.dasi.type.SystemConstant.*;

@RestController
@RequestMapping("/api/v1/query")
@Slf4j
public class QueryController implements IQueryService {

    @Resource
    private RedissonClient redissonClient;

    @GetMapping("/tags")
    @Override
    public Result<List<String>> queryRagTagList() {
        RList<String> elements = redissonClient.getList(REDIS_RAG_TAG_LIST_KEY);
        return Result.success(elements);
    }

    @GetMapping("/models")
    @Override
    public Result<List<String>> queryChatModelList() {
        RList<String> elements = redissonClient.getList(REDIS_CHAT_MODEL_LIST_KEY);
        return Result.success(elements);
    }

}
