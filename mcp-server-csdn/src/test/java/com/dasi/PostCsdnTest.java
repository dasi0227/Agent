package com.dasi;

import com.dasi.sse.dto.PostCsdnHttpRequest;
import com.dasi.sse.dto.PostCsdnHttpResponse;
import com.dasi.sse.gateway.IPostCsdnHttp;
import com.dasi.type.properties.PostCsdnProperties;
import com.dasi.type.util.MarkdownConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import retrofit2.Response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = McpServerCsdnApplication.class)
class PostCsdnTest {

    @Autowired
    private IPostCsdnHttp postCsdnHttp;

    @Autowired
    private PostCsdnProperties postCsdnProperties;

    @Test
    void publishArticle_shouldReturnUrlAndQrcode() throws Exception {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String markdown = "# MCP CSDN 测试\n\n这是一篇自动化发布测试。\n\n时间: " + timestamp;

        PostCsdnHttpRequest request = new PostCsdnHttpRequest();
        request.setTitle("MCP CSDN 测试-" + timestamp);
        request.setMarkdowncontent(markdown);
        request.setContent(MarkdownConverter.convertToHtml(markdown));
        request.setDescription("MCP 自动化发布测试 " + timestamp);
        request.setTags(postCsdnProperties.getTags());
        request.setCategories(postCsdnProperties.getCategories());

        Response<PostCsdnHttpResponse> response = postCsdnHttp
                .saveArticle(request, postCsdnProperties.getCookie())
                .execute();

        if (!response.isSuccessful()) {
            String errorBody = response.errorBody() == null ? "<empty>" : response.errorBody().string();
            fail("HTTP " + response.code() + ": " + errorBody);
        }

        PostCsdnHttpResponse body = response.body();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo(200);
        assertThat(body.getData()).isNotNull();
        assertThat(body.getData().getUrl()).isNotBlank();
        assertThat(body.getData().getQrcode()).isNotBlank();

        System.out.println("CSDN URL: " + body.getData().getUrl());
        System.out.println("CSDN QRCode: " + body.getData().getQrcode());
    }
}
