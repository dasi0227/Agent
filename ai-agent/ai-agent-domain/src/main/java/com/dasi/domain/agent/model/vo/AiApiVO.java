package com.dasi.domain.agent.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiApiVO {

    /** 接口 id */
    private String apiId;

    /** 基础路径 */
    private String apiBaseUrl;

    /** 密钥 */
    private String apiKey;

    /** 对话路径 */
    private String apiCompletionsPath;

    /** 嵌入路径 */
    private String apiEmbeddingsPath;

}
