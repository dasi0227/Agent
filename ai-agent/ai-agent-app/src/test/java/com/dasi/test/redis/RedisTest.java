package com.dasi.test.redis;

import com.dasi.infrastructure.persistent.po.AiApi;
import com.dasi.infrastructure.persistent.po.AiClient;
import com.dasi.domain.util.IRedisService;
import com.dasi.types.dto.response.ChatClientResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class RedisTest {

    @Resource
    private IRedisService redisService;

    @Test
    public void testRedisValueTypes() {

        String listKey = "test:redis:list";
        String mapKey = "test:redis:map";
        String stringKey = "test:redis:string";
        String intKey = "test:redis:int";
        String longKey = "test:redis:long";
        String apiKey = "test:redis:aiApi";
        String modelKey = "test:redis:chatClientResponse";
        String clientListKey = "test:redis:clientList";

        List<String> listValue = Arrays.asList("a", "b", "c");
        Map<String, Object> mapValue = new HashMap<>();
        mapValue.put("name", "dasi");
        mapValue.put("age", 18);
        AiApi apiValue = AiApi.builder()
                .id(1L)
                .apiId("api_test")
                .apiBaseUrl("http://localhost")
                .apiKey("key")
                .apiCompletionsPath("/v1/chat/completions")
                .apiEmbeddingsPath("/v1/embeddings")
                .apiStatus(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        ChatClientResponse modelValue = ChatClientResponse.builder()
                .clientId("client_test")
                .clientName("Test Client")
                .build();
        List<AiClient> clientListValue = Arrays.asList(
                AiClient.builder()
                        .id(10L)
                        .clientId("client_a")
                        .clientName("Client A")
                        .clientDesc("desc a")
                        .clientStatus(1)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build(),
                AiClient.builder()
                        .id(11L)
                        .clientId("client_b")
                        .clientName("Client B")
                        .clientDesc("desc b")
                        .clientStatus(1)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build()
        );

        redisService.setStringValue(listKey, listValue);
        redisService.setStringValue(mapKey, mapValue);
        redisService.setStringValue(stringKey, "hello");
        redisService.setStringValue(intKey, 123);
        redisService.setStringValue(longKey, 123456789L);
        redisService.setStringValue(apiKey, apiValue);
        redisService.setStringValue(modelKey, modelValue);
        redisService.setStringValue(clientListKey, clientListValue);

        List<String> listResult = redisService.getStringValue(listKey);
        Map<String, Object> mapResult = redisService.getStringValue(mapKey);
        String stringResult = redisService.getStringValue(stringKey);
        Integer intResult = redisService.getStringValue(intKey);
        Long longResult = redisService.getStringValue(longKey);
        AiApi apiResult = redisService.getStringValue(apiKey);
        ChatClientResponse modelResult = redisService.getStringValue(modelKey);
        List<AiClient> clientListResult = redisService.getStringValue(clientListKey);

        log.info("listResult={}", listResult);
        log.info("mapResult={}", mapResult);
        log.info("stringResult={}", stringResult);
        log.info("intResult={}", intResult);
        log.info("longResult={}", longResult);
        log.info("apiResult={}", apiResult);
        log.info("modelResult={}", modelResult);
        log.info("clientListResult={}", clientListResult);

        Assert.assertEquals(listValue, listResult);
        Assert.assertEquals(mapValue, mapResult);
        Assert.assertEquals("hello", stringResult);
        Assert.assertEquals(Integer.valueOf(123), intResult);
        Assert.assertEquals(Long.valueOf(123456789L), longResult);
        Assert.assertEquals(apiValue, apiResult);
        Assert.assertEquals(modelValue, modelResult);
        Assert.assertEquals(clientListValue, clientListResult);
    }
}
