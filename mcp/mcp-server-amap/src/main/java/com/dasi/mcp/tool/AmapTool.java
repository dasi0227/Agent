package com.dasi.mcp.tool;

import com.dasi.mcp.adapter.IAmapPort;
import com.dasi.mcp.dto.CheckWeatherToolRequest;
import com.dasi.mcp.dto.CheckWeatherToolResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Service
public class AmapTool {

    @Resource
    private IAmapPort amapPort;

    @Tool(description = "通过高德地图获取城市的天气信息")
    public CheckWeatherToolResponse checkWeather(CheckWeatherToolRequest toolRequest) throws IOException {
        log.info("调用 MCP 工具进行高德地图获取天气信息：address={}, date={}", toolRequest.getAddress(), LocalDateTime.now());
        return amapPort.checkWeather(toolRequest);
    }

}
