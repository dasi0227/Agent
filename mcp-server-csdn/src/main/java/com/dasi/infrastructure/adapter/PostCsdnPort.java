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
        serviceRequest.setTags(postCsdnProperties.getTags());
        serviceRequest.setCategories(postCsdnProperties.getCategories());

        Call<PostCsdnServiceResponse> call = postCsdnService.saveArticle(serviceRequest, postCsdnProperties.getCookie());
        Response<PostCsdnServiceResponse> result = call.execute();

        if (result.isSuccessful()) {
            PostCsdnServiceResponse serviceResponse = result.body();
            if (serviceResponse == null) return null;

            PostCsdnToolResponse toolResponse = new PostCsdnToolResponse();
            toolResponse.setCode(serviceResponse.getCode());
            toolResponse.setMsg(serviceResponse.getMsg());
            toolResponse.setUrl(serviceResponse.getData().getUrl());
            toolResponse.setId(serviceResponse.getData().getId());
            toolResponse.setQrcode(serviceResponse.getData().getQrcode());

            return toolResponse;
        }

        return null;
    }

}
