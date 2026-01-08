package com.dasi.mcp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

/**
 * MCP 调用工具的响应结果
 */
@Data
public class PostCsdnToolResponse {

    @JsonProperty(required = true, value = "code")
    @JsonPropertyDescription("发送文章的结果码")
    private Integer code;

    @JsonProperty(required = true, value = "msg")
    @JsonPropertyDescription("发送文章的结果信息")
    private String msg;

    @JsonProperty(required = true, value = "url")
    @JsonPropertyDescription("文章的发布网址")
    private String url;

    @JsonProperty(required = true, value = "id")
    @JsonPropertyDescription("文章的发布id")
    private Long id;

    @JsonProperty(required = true, value = "qrcode")
    @JsonPropertyDescription("文章的发布二维码")
    private String qrcode;

}
