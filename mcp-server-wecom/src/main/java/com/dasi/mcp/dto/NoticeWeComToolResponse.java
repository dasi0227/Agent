package com.dasi.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoticeWeComToolResponse {

    @JsonProperty(required = true, value = "code")
    @JsonPropertyDescription("企业微信调用状态码")
    private Integer code;

    @JsonProperty(required = true, value = "info")
    @JsonPropertyDescription("企业微信调用状态信息")
    private String info;

    @JsonProperty(required = true, value = "msgid")
    @JsonPropertyDescription("企业微信发送的消息 id")
    private String msgid;

}
