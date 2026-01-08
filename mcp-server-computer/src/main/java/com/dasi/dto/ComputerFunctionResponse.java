package com.dasi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComputerFunctionResponse {

    @JsonProperty(required = true, value = "computerName")
    @JsonPropertyDescription("电脑名称")
    private String computerName;

    @JsonProperty(required = true, value = "osName")
    @JsonPropertyDescription("操作系统名称")
    private String osName;

    @JsonProperty(required = true, value = "osVersion")
    @JsonPropertyDescription("操作系统版本")
    private String osVersion;

    @JsonProperty(required = true, value = "osArch")
    @JsonPropertyDescription("操作系统架构")
    private String osArch;

    @JsonProperty(required = true, value = "userName")
    @JsonPropertyDescription("用户的账户名称")
    private String userName;

    @JsonProperty(required = true, value = "userHome")
    @JsonPropertyDescription("用户的主目录")
    private String userHome;

    @JsonProperty(required = true, value = "userDir")
    @JsonPropertyDescription("用户的当前工作目录")
    private String userDir;

    @JsonProperty(required = true, value = "javaVersion")
    @JsonPropertyDescription("Java 运行时环境版本")
    private String javaVersion;

}
