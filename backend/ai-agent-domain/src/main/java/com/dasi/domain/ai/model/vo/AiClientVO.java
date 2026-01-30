package com.dasi.domain.ai.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientVO {

    /**
     * 客户端 id
     */
    private String clientId;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 客户端类型：chat/work
     */
    private String clientType;

    /**
     * 客户端描述
     */
    private String clientDesc;

    /**
     * 模型 id
     */
    private String modelId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 提示词 ID List
     */
    private List<String> promptIdList;

    /**
     * 工具 ID List
     */
    private List<String> mcpIdList;

    /**
     * 顾问 ID List
     */
    private List<String> advisorIdList;

}
