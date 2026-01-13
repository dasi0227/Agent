package com.dasi.domain.agent.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAdvisorVO {

    /** 顾问 id */
    private String advisorId;

    /** 顾问名称 */
    private String advisorName;

    /** 顾问类型 */
    private String advisorType;

    /** 顾问顺序号 */
    private Integer advisorOrder;

    /** 对话记忆 */
    private ChatMemory chatMemory;

    /** 知识库回答 */
    private RagAnswer ragAnswer;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatMemory {
        private int maxMessages;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RagAnswer {
        private int topK = 4;
        private String filterExpression;
    }

}
