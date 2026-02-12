package com.dasi.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendTextToolRequest {

    @JsonProperty(required = true, value = "content")
    @JsonPropertyDescription("文本内容，支持换行和 A 标签，但是换行符需要使用转义过的 \\n")
    private String content;

}
