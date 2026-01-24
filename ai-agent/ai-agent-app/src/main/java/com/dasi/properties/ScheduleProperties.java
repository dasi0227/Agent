package com.dasi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "schedule",  ignoreInvalidFields = true)
public class ScheduleProperties {

    private String refreshCron;

}
