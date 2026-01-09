package com.dasi.infrastructure.adapter;

import com.dasi.infrastructure.dto.PostCsdnServiceRequest;
import com.dasi.infrastructure.dto.PostCsdnServiceResponse;
import com.dasi.infrastructure.gateway.IPostCsdnService;
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
    private IPostCsdnService postCsdnService;

    @Resource
    private PostCsdnProperties postCsdnProperties;

    @Override
    public PostCsdnToolResponse saveArticle(PostCsdnToolRequest toolRequest) throws IOException {

        PostCsdnServiceRequest serviceRequest = new PostCsdnServiceRequest();
        serviceRequest.setTitle(toolRequest.getTitle());
        serviceRequest.setMarkdowncontent(toolRequest.getMarkdowncontent());
        serviceRequest.setContent(toolRequest.getContent());
        serviceRequest.setDescription(toolRequest.getDescription());
        serviceRequest.setCover_images(List.of(postCsdnProperties.getCoverUrl()));
        serviceRequest.setTags(postCsdnProperties.getTags());
        serviceRequest.setCategories(postCsdnProperties.getCategories());

        Call<PostCsdnServiceResponse> call = postCsdnService.saveArticle(serviceRequest, postCsdnProperties.getCookie());
        Response<PostCsdnServiceResponse> result = call.execute();

        log.info("通过 HTTP 请求向 CSDN 发帖：标题={}", toolRequest.getTitle());

        if (!result.isSuccessful()) {
            String err = result.errorBody() == null ? "<empty>" : result.errorBody().string();
            PostCsdnToolResponse r = new PostCsdnToolResponse();
            r.setCode(result.code());
            r.setMsg("CSDN HTTP 请求失败: " + err);
            return r;
        }

        PostCsdnServiceResponse serviceResponse = result.body();
        PostCsdnToolResponse toolResponse = new PostCsdnToolResponse();

        if (serviceResponse == null) {
            toolResponse.setCode(500);
            toolResponse.setMsg("CSDN 响应体为空");
            return toolResponse;
        }
        if (serviceResponse.getData() == null) {
            toolResponse.setCode(serviceResponse.getCode());
            toolResponse.setMsg("CSDN 响应数据为空: " + serviceResponse.getMsg());
            return toolResponse;
        }

        toolResponse.setCode(serviceResponse.getCode());
        toolResponse.setMsg(serviceResponse.getMsg());
        toolResponse.setUrl(serviceResponse.getData().getUrl());
        toolResponse.setId(serviceResponse.getData().getId());
        toolResponse.setQrcode(serviceResponse.getData().getQrcode());

        return toolResponse;
    }

}
