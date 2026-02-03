package com.dasi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 接口配置表 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiApi {
    /** 自增 id */
    private Long id;

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

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
