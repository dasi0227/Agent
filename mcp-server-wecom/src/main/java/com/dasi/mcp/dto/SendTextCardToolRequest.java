package com.dasi.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendTextCardToolRequest {

    @JsonProperty(required = true, value = "title")
    @JsonPropertyDescription("主题")
    private String title;

    @JsonProperty(required = true, value = "description")
    @JsonPropertyDescription("描述")
    private String description;

    @JsonProperty(required = true, value = "url")
    @JsonPropertyDescription("跳转地址")
    private String url;

}
