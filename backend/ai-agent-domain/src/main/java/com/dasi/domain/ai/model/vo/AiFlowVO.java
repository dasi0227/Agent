package com.dasi.domain.ai.model.vo;

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

    /** 客户端角色 */
    private String clientRole;

    /** 工作流提示词 */
    private String flowPrompt;

    /** 工作流顺序 */
    private Integer flowSeq;

}
