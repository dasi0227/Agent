package com.dasi;

import com.dasi.mcp.dto.NoticeWeComToolRequest;
import com.dasi.mcp.dto.NoticeWeComToolResponse;
import com.dasi.mcp.tool.NoticeWeComTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = McpServerWeComApplication.class)
class NoticeWeComSendMessageTest {

    @Autowired
    private NoticeWeComTool noticeWeComTool;

    @Test
    void sendTextCard() throws Exception {
        NoticeWeComToolRequest request = new NoticeWeComToolRequest();
        request.setTitle("Test Notice");
        request.setDescription("Test notice from mcp-server-wecom");
        request.setUrl("https://example.com");

        NoticeWeComToolResponse response = noticeWeComTool.noticeArticle(request);
        System.out.println("WeCom response: " + response);
        Assertions.assertNotNull(response, "WeCom response is null");
        Assertions.assertEquals(0, response.getCode(), "WeCom send failed: " + response.getInfo());
    }

}
