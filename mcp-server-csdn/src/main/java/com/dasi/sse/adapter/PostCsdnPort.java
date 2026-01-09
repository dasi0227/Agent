package com.dasi.sse.adapter;

import com.dasi.sse.dto.PostCsdnHttpRequest;
import com.dasi.sse.dto.PostCsdnHttpResponse;
import com.dasi.sse.gateway.IPostCsdnHttp;
import com.dasi.mcp.adapter.IPostCsdnPort;
import com.dasi.mcp.dto.PostCsdnToolRequest;
import com.dasi.mcp.dto.PostCsdnToolResponse;
import com.dasi.type.properties.PostCsdnProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class PostCsdnPort implements IPostCsdnPort {

    @Resource
    private IPostCsdnHttp postCsdnHttp;

    @Resource
    private PostCsdnProperties postCsdnProperties;

    @Override
    public PostCsdnToolResponse saveArticle(PostCsdnToolRequest toolRequest) throws IOException {

        PostCsdnHttpRequest httpRequest = new PostCsdnHttpRequest();
        httpRequest.setTitle(toolRequest.getTitle());
        httpRequest.setMarkdowncontent(toolRequest.getMarkdowncontent());
        httpRequest.setContent(toolRequest.getContent());
        httpRequest.setCover_images(List.of(postCsdnProperties.getCoverUrl()));
        httpRequest.setTags(postCsdnProperties.getTags());
        httpRequest.setCategories(postCsdnProperties.getCategories());

        Call<PostCsdnHttpResponse> call = postCsdnHttp.saveArticle(httpRequest, postCsdnProperties.getCookie());
        Response<PostCsdnHttpResponse> result = call.execute();

        log.info("调用 HTTP 进行 CSDN 发帖：标题={}", toolRequest.getTitle());

        PostCsdnToolResponse toolResponse = new PostCsdnToolResponse();

        if (!result.isSuccessful()) {
            String err = result.errorBody() == null ? "<empty>" : result.errorBody().string();
            toolResponse.setCode(result.code());
            toolResponse.setMsg("CSDN HTTP 请求失败: " + err);
            return toolResponse;
        }

        PostCsdnHttpResponse httpResponse = result.body();

        if (httpResponse == null) {
            toolResponse.setCode(500);
            toolResponse.setMsg("CSDN HTTP 响应体为空");
            return toolResponse;
        }
        if (httpResponse.getData() == null) {
            toolResponse.setCode(httpResponse.getCode());
            toolResponse.setMsg("CSDN HTTP 响应数据为空: " + httpResponse.getMsg());
            return toolResponse;
        }

        toolResponse.setCode(httpResponse.getCode());
        toolResponse.setMsg(httpResponse.getMsg());
        toolResponse.setUrl(httpResponse.getData().getUrl());
        toolResponse.setId(httpResponse.getData().getId());
        toolResponse.setQrcode(httpResponse.getData().getQrcode());

        return toolResponse;
    }

}
