package com.dasi.domain.agent.service.execute.loop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.dasi.domain.agent.model.enumeration.AiClientType.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteLoopResult {

    private String clientType;

    private String sectionType;

    private String sectionContent;

    private Integer step;

    private Boolean completed;

    private Long timestamp;

    private String sessionId;

    public static ExecuteLoopResult createAnalyzerResult(String sectionType, String sectionContent, Integer step, String sessionId) {
        return createResult(ANALYZER.getType(), sectionType, sectionContent, step, false, sessionId);
    }

    public static ExecuteLoopResult createPerformerResult(String sectionType, String sectionContent, Integer step, String sessionId) {
        return createResult(PERFORMER.getType(), sectionType, sectionContent, step, false, sessionId);
    }

    public static ExecuteLoopResult createSupervisorResult(String sectionType, String sectionContent, Integer step, String sessionId) {
        return createResult(SUPERVISOR.getType(), sectionType, sectionContent, step, false, sessionId);
    }

    public static ExecuteLoopResult createSummarizerResult(String sectionType, String sectionContent, Integer step, String sessionId) {
        return createResult(SUMMARIZER.getType(), sectionType, sectionContent, step, false, sessionId);
    }

    public static ExecuteLoopResult createCompleteResult(String sectionContent, String sessionId) {
        return createResult("complete", null, sectionContent, null, true, sessionId);
    }

    public static ExecuteLoopResult createResult(String clientType, String sectionType, String sectionContent, Integer step, Boolean completed, String sessionId) {
        return ExecuteLoopResult.builder()
                .clientType(clientType)
                .sectionType(sectionType)
                .sectionContent(sectionContent)
                .step(step)
                .completed(completed)
                .timestamp(System.currentTimeMillis())
                .sessionId(sessionId)
                .build();
    }



}
