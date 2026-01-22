package com.dasi.test.domain;

import com.dasi.domain.agent.service.armory.ArmoryStrategyFactory;
import com.dasi.domain.agent.service.execute.loop.ExecuteLoopStrategy;
import com.dasi.infrastructure.persistent.dao.IAiPromptDao;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class ExecuteTest {

    @Resource
    private ArmoryStrategyFactory armoryStrategyFactory;

    @Resource
    private ExecuteLoopStrategy executeLoopStrategy;

    @Resource
    private IAiPromptDao aiPromptDao;


}
