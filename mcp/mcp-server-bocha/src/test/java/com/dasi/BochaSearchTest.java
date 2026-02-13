package com.dasi;

import com.dasi.mcp.dto.BochaSearchToolRequest;
import com.dasi.mcp.dto.BochaSearchToolResponse;
import com.dasi.mcp.tool.BochaTool;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest(classes = McpServerBochaApplication.class)
public class BochaSearchTest {

    @Resource
    private BochaTool bochaTool;

    @Test
    public void testTool() throws IOException {
        BochaSearchToolRequest request = new BochaSearchToolRequest();
        request.setQuery("2026-02-12 日的 AI 新闻");
        request.setFreshness("noLimit");
        BochaSearchToolResponse response = bochaTool.webSearch(request);
        System.out.println("BOCHA response\n" + response);
    }

}
