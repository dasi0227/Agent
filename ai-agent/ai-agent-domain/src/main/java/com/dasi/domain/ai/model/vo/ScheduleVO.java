package com.dasi.domain.ai.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.scheduling.support.CronTrigger;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleVO {

    private String scheduleId;

    private CronTrigger scheduleCron;

    private Runnable scheduleExecutor;

    private String scheduleSignature;

}
