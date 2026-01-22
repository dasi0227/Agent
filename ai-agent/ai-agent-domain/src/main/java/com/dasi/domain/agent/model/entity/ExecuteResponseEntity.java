package com.dasi.domain.agent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.dasi.domain.agent.model.enumeration.AiClientType.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteResponseEntity {

    private String clientType;

    private String sectionType;

    private String sectionContent;

    private Integer step;

    private Boolean completed;

    private Long timestamp;

    private String sessionId;

    public static ExecuteResponseEntity createAnalyzerResponse(String sectionType, String sectionContent, Integer step, String sessionId) {
        return createResponse(ANALYZER.getType(), sectionType, sectionContent, step, false, sessionId);
    }

    public static ExecuteResponseEntity createPerformerResponse(String sectionType, String sectionContent, Integer step, String sessionId) {
        return createResponse(PERFORMER.getType(), sectionType, sectionContent, step, false, sessionId);
    }

    public static ExecuteResponseEntity createSupervisorResponse(String sectionType, String sectionContent, Integer step, String sessionId) {
        return createResponse(SUPERVISOR.getType(), sectionType, sectionContent, step, false, sessionId);
    }

    public static ExecuteResponseEntity createSummarizerResponse(String sectionType, String sectionContent, Integer step, String sessionId) {
        return createResponse(SUMMARIZER.getType(), sectionType, sectionContent, step, false, sessionId);
    }

    public static ExecuteResponseEntity createCompleteResponse(String sectionContent, String sessionId) {
        return createResponse("complete", null, sectionContent, null, true, sessionId);
    }

    public static ExecuteResponseEntity createResponse(String clientType, String sectionType, String sectionContent, Integer step, Boolean completed, String sessionId) {
        return ExecuteResponseEntity.builder()
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
