package com.dasi.mcp.dto;

import com.dasi.type.util.MarkdownConverter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

/**
 * MCP 调用工具的请求参数
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaveArticleToolRequest {

    @JsonProperty(required = true, value = "title")
    @JsonPropertyDescription("文章标题")
    private String title;

    @JsonProperty(required = true, value = "markdownContent")
    @JsonPropertyDescription("文章内容")
    private String markdownContent;

    public String getHtmlContent() {
        return MarkdownConverter.convertToHtml(markdownContent);
    }

}
