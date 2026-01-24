package com.dasi.mcp.dto;

import com.dasi.type.model.WeatherCondition;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckWeatherToolResponse {

    @JsonProperty(required = true, value = "code")
    @JsonPropertyDescription("高德地图调用状态码")
    private String code;

    @JsonProperty(required = true, value = "info")
    @JsonPropertyDescription("高德地图调用状态信息")
    private String info;

    @JsonProperty(required = true, value = "province")
    @JsonPropertyDescription("当前地址的省份")
    private String province;

    @JsonProperty(required = true, value = "city")
    @JsonPropertyDescription("当前地址的城市")
    private String city;

    @JsonProperty(required = true, value = "reportTime")
    @JsonPropertyDescription("天气信息的发布时间，格式 yyyy-MM-dd hh:mm:ss")
    private String reportTime;

    @JsonProperty(required = true, value = "weatherCondition")
    @JsonPropertyDescription("当天天气信息")
    private WeatherCondition todayWeather;

    @JsonProperty(required = true, value = "tomorrowWeather")
    @JsonPropertyDescription("明天天气信息")
    private WeatherCondition tomorrowWeather;

}
