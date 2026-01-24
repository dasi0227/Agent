package com.dasi.type.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherCondition {

    @JsonProperty(value = "date")
    @JsonPropertyDescription("日期，格式 yyyy-MM-dd")
    private String date;

    @JsonProperty(value = "dayWeather")
    @JsonPropertyDescription("白天天气现象")
    private String dayWeather;

    @JsonProperty(value = "nightWeather")
    @JsonPropertyDescription("夜间天气现象")
    private String nightWeather;

    @JsonProperty(value = "dayTemperature")
    @JsonPropertyDescription("白天温度，单位：°C")
    private String dayTemperature;

    @JsonProperty(value = "nightTemperature")
    @JsonPropertyDescription("夜间温度，单位：°C")
    private String nightTemperature;

    @JsonProperty(value = "dayWindDirection")
    @JsonPropertyDescription("白天风向")
    private String dayWindDirection;

    @JsonProperty(value = "nightWindDirection")
    @JsonPropertyDescription("夜间风向")
    private String nightWindDirection;

    @JsonProperty(value = "dayWindPower")
    @JsonPropertyDescription("白天风力等级")
    private String dayWindPower;

    @JsonProperty(value = "nightWindPower")
    @JsonPropertyDescription("夜间风力等级")
    private String nightWindPower;

}
