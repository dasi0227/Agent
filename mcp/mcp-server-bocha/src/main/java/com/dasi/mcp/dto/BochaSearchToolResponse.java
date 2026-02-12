package com.dasi.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BochaSearchToolResponse {

    @JsonProperty(required = true, value = "code")
    @JsonPropertyDescription("博查调用的状态码")
    private String code;

    @JsonProperty(required = true, value = "info")
    @JsonPropertyDescription("博查调用的状态信息")
    private String info;

    @JsonProperty(required = true, value = "results")
    @JsonPropertyDescription("网络搜索的结果列表")
    private List<SearchResult> results;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SearchResult {

        @JsonProperty(required = true, value = "name")
        @JsonPropertyDescription("网页标题")
        private String name;

        @JsonProperty(required = true, value = "url")
        @JsonPropertyDescription("网页 URL")
        private String url;

        @JsonProperty(required = true, value = "snippet")
        @JsonPropertyDescription("网页简短描述")
        private String snippet;

        @JsonProperty(required = true, value = "summary")
        @JsonPropertyDescription("网页摘要")
        private String summary;

        @JsonProperty(required = true, value = "datePublished")
        @JsonPropertyDescription("网页发布时间")
        private String datePublished;
    }

}
