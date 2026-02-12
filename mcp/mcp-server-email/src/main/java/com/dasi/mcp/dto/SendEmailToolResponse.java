package com.dasi.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendEmailToolResponse {

    @JsonProperty(required = true, value = "code")
    @JsonPropertyDescription("邮件发送状态码")
    private Integer code;

    @JsonProperty(required = true, value = "info")
    @JsonPropertyDescription("邮件发送状态信息")
    private String info;

    @JsonProperty(required = true, value = "messageId")
    @JsonPropertyDescription("邮件消息ID")
    private String messageId;

}
