package com.dasi;

import com.dasi.infrastructure.dto.PostCsdnServiceRequest;
import com.dasi.infrastructure.dto.PostCsdnServiceResponse;
import com.dasi.infrastructure.gateway.IPostCsdnService;
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

@SpringBootTest(classes = McpServerApplication.class)
class PostCsdnTest {

    @Autowired
    private IPostCsdnService postCsdnService;

    @Autowired
    private PostCsdnProperties postCsdnProperties;

    @Test
    void publishArticle_shouldReturnUrlAndQrcode() throws Exception {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String markdown = "# MCP CSDN 测试\n\n这是一篇自动化发布测试。\n\n时间: " + timestamp;

        PostCsdnServiceRequest request = new PostCsdnServiceRequest();
        request.setTitle("MCP CSDN 测试-" + timestamp);
        request.setMarkdowncontent(markdown);
        request.setContent(MarkdownConverter.convertToHtml(markdown));
        request.setDescription("MCP 自动化发布测试 " + timestamp);
        request.setTags(postCsdnProperties.getTags());
        request.setCategories(postCsdnProperties.getCategories());

        Response<PostCsdnServiceResponse> response = postCsdnService
                .saveArticle(request, postCsdnProperties.getCookie())
                .execute();

        if (!response.isSuccessful()) {
            String errorBody = response.errorBody() == null ? "<empty>" : response.errorBody().string();
            fail("HTTP " + response.code() + ": " + errorBody);
        }

        PostCsdnServiceResponse body = response.body();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo(200);
        assertThat(body.getData()).isNotNull();
        assertThat(body.getData().getUrl()).isNotBlank();
        assertThat(body.getData().getQrcode()).isNotBlank();

        System.out.println("CSDN URL: " + body.getData().getUrl());
        System.out.println("CSDN QRCode: " + body.getData().getQrcode());
    }
}
