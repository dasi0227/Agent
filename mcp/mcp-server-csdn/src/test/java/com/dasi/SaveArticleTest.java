package com.dasi;

import com.dasi.mcp.dto.SaveArticleToolRequest;
import com.dasi.mcp.dto.SaveArticleToolResponse;
import com.dasi.mcp.tool.CsdnTool;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = McpServerCsdnApplication.class)
public class SaveArticleTest {

    @Resource
    private CsdnTool csdnTool;

    @Test
    void testTool() throws Exception {
        String title = "MCP CSDN 测试";
        String markdownContent = "MCP CSDN 测试";

        SaveArticleToolRequest request = new SaveArticleToolRequest();
        request.setTitle(title);
        request.setMarkdownContent(markdownContent);

        SaveArticleToolResponse response = csdnTool.saveArticle(request);
        System.out.println("Csdn response\n" + response);
    }
}
