package com.dasi.infrastructure.persistent.po;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    /** 状态：0-禁用，1-启用 */
    private Integer apiStatus;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
