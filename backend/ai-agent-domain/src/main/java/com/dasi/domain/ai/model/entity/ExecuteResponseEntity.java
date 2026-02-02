package com.dasi.domain.ai.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.dasi.domain.ai.model.enumeration.AiClientRole.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteResponseEntity {

    private String clientType;

    private String sectionType;

    private String sectionContent;

    private Integer round;

    private Integer step;

    private Long timestamp;

    private String sessionId;

    public static ExecuteResponseEntity createAnalyzerResponse(String sectionType, String sectionContent, Integer round, String sessionId) {
        return createResponse(ANALYZER.getRole(), sectionType, sectionContent, round, null, sessionId);
    }

    public static ExecuteResponseEntity createPerformerResponse(String sectionType, String sectionContent, Integer round, String sessionId) {
        return createResponse(PERFORMER.getRole(), sectionType, sectionContent, round, null, sessionId);
    }

    public static ExecuteResponseEntity createSupervisorResponse(String sectionType, String sectionContent, Integer round, String sessionId) {
        return createResponse(SUPERVISOR.getRole(), sectionType, sectionContent, round, null, sessionId);
    }

    public static ExecuteResponseEntity createSummarizerResponse(String sectionType, String sectionContent, Integer round, String sessionId) {
        return createResponse(SUMMARIZER.getRole(), sectionType, sectionContent, round, null, sessionId);
    }

    public static ExecuteResponseEntity createInspectorResponse(String sectionType, String sectionContent, String sessionId) {
        return createResponse(INSPECTOR.getRole(), sectionType, sectionContent, null, null, sessionId);
    }

    public static ExecuteResponseEntity createPlannerResponse(String sectionType, String sectionContent, String sessionId) {
        return createResponse(PLANNER.getRole(), sectionType, sectionContent, null, null, sessionId);
    }

    public static ExecuteResponseEntity createRunnerResponse(String sectionType, String sectionContent, Integer step, String sessionId) {
        return createResponse(RUNNER.getRole(), sectionType, sectionContent, null, step, sessionId);
    }

    public static ExecuteResponseEntity createReplierResponse(String sectionType, String sectionContent, String sessionId) {
        return createResponse(REPLIER.getRole(), sectionType, sectionContent, null, null, sessionId);
    }

    public static ExecuteResponseEntity createCompleteResponse(String sectionContent, String sessionId) {
        return createResponse("complete", null, sectionContent, null, null, sessionId);
    }

    public static ExecuteResponseEntity createResponse(String clientType, String sectionType, String sectionContent, Integer round, Integer step, String sessionId) {
        return ExecuteResponseEntity.builder()
                .clientType(clientType)
                .sectionType(sectionType)
                .sectionContent(sectionContent)
                .round(round)
                .step(step)
                .timestamp(System.currentTimeMillis())
                .sessionId(sessionId)
                .build();
    }

}
