package com.dasi.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckWeatherToolRequest {

    @JsonProperty(required = true, value = "cityName")
    @JsonPropertyDescription("地理位置，填写结构化地址信息：省份＋城市＋区县＋城镇＋乡村＋街道＋门牌号码")
    private String address;

}
