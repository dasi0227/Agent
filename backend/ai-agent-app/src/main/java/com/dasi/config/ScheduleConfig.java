package com.dasi.config;

import com.dasi.properties.ScheduleProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@Configuration
@EnableConfigurationProperties(ScheduleProperties.class)
@EnableScheduling
public class ScheduleConfig {

}
