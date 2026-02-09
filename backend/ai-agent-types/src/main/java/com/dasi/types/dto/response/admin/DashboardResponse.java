package com.dasi.types.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private CountInfo countInfo;

    private GraphInfo graphInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphInfo {
        /** 消息数量（最近 7 天） */
        private List<ChartValue> messageLastWeek;

        /** 消息数量（最近 30 天） */
        private List<ChartValue> messageLastMonth;

        /** work 使用次数（按配置项维度） */
        private Map<String, List<BarValue>> workUsage;

        /** chat 使用次数（按配置项维度） */
        private Map<String, List<BarValue>> chatUsage;

        /** session 比例：work:chat */
        private PieValue sessionWorkVsChat;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountInfo {
        private Integer apiCount;
        private Integer modelCount;
        private Integer clientCount;
        private Integer agentCount;
        private Integer promptCount;
        private Integer advisorCount;
        private Integer mcpCount;
        private Integer configCount;
        private Integer flowCount;
        private Integer userCount;
        private Integer sessionCount;
        private Integer messageCount;
        private Integer taskCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartValue {
        private String date;
        private Integer count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BarValue {
        private String id;
        private Integer value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PieValue {
        private Integer workCount;
        private Integer chatCount;
    }

}
