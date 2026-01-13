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

        String apiId = "test";

        AiApi result = aiApiDao.queryByApiId(apiId);

        log.info("查询结果：{}", result);
    }

}
