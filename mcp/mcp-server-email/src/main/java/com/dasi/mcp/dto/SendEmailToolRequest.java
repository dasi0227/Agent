package com.dasi.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendEmailToolRequest {

    @JsonProperty(required = true, value = "to")
    @JsonPropertyDescription("收件邮箱地址")
    private String to;

    @JsonProperty(required = true, value = "subject")
    @JsonPropertyDescription("邮件主题")
    private String subject;

    @JsonProperty(required = true, value = "content")
    @JsonPropertyDescription("邮件正文内容")
    private String content;

    @JsonProperty(required = true, value = "html")
    @JsonPropertyDescription("是否以 HTML 形式发送，true 表示 HTML，false 表示纯文本，默认为 true")
    private Boolean html = true;

}
