package com.dasi;

import com.dasi.mcp.dto.CheckWeatherToolRequest;
import com.dasi.mcp.dto.CheckWeatherToolResponse;
import com.dasi.mcp.tool.AmapTool;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest(classes = McpServerAmapApplication.class)
public class CheckWeatherTest {

    @Resource
    private AmapTool amapTool;

    @Test
    public void testTool() throws IOException {
        CheckWeatherToolRequest request = new CheckWeatherToolRequest();
        request.setAddress("广东省广州市天河区");
        CheckWeatherToolResponse response = amapTool.checkWeather(request);
        System.out.println("AMAP response\n" + response);
    }

}
