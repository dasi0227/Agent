package com.dasi.domain.agent.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiFlowVO {

    /** 智能体ID */
    private String agentId;

    /** 客户端ID */
    private String clientId;

    /** 客户端ID */
    private String clientName;

    /** 客户端ID */
    private String clientType;

    /** 工作流提示词 */
    private String flowPrompt;

    /** 工作流顺序 */
    private Integer flowSeq;

}
