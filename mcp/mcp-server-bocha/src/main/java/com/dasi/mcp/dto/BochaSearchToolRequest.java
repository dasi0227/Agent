package com.dasi.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BochaSearchToolRequest {

    @JsonProperty(required = true, value = "query")
    @JsonPropertyDescription("网络搜索的提示词")
    private String query;

    @JsonProperty(required = true, value = "freshness")
    @JsonPropertyDescription("时间范围：noLimit/oneDay/oneWeek/oneMonth/oneYear/YYYY-MM-DD/YYYY-MM-DD..YYYY-MM-DD")
    private String freshness;

}
