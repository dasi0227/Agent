package com.dasi.domain.agent.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiMcpVO {

    /** 工具 id */
    private String mcpId;

    /** 工具名称 */
    private String mcpName;

    /** 工具类型 */
    private String mcpType;

    /** 工具路径 */
    private String mcpConfig;

    /** 请求超时时间 */
    private Integer mcpTimeout;

    /** 传输配置 - sse */
    private SseConfig sseConfig;

    /** 传输配置 - stdio */
    private StdioConfig stdioConfig;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SseConfig {
        private String baseUri;
        private String sseEndPoint;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StdioConfig {

        private Map<String, Stdio> stdio;

        @Data
        public static class Stdio {
            private String command;
            private List<String> args;
            private Map<String, String> env;
        }
    }

}
