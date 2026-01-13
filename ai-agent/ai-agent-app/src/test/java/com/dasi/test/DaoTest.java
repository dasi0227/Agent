package com.dasi.test;

import com.dasi.infrastructure.persistent.dao.IAiApiDao;
import com.dasi.infrastructure.persistent.po.AiApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class DaoTest {

    @Resource
    private IAiApiDao aiApiDao;

    @Test
    public void testAiApiDao() {

        String apiId = "test-" + System.currentTimeMillis();

        AiApi aiApi = new AiApi();
        aiApi.setApiId(apiId);
        aiApi.setApiBaseUrl("https://api.test.com");
        aiApi.setApiKey("test-key-123");
        aiApi.setApiCompletionsPath("/v1/chat/completions");
        aiApi.setApiEmbeddingsPath("/v1/embeddings");
        aiApi.setApiStatus(1);

        aiApiDao.insertAiAPI(aiApi);

        AiApi result = aiApiDao.selectByApiId(apiId);

        log.info("查询结果：{}", result);
    }

}
