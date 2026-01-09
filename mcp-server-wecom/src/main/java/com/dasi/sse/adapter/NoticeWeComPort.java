package com.dasi.sse.adapter;

import com.dasi.mcp.adapter.INoticeWeiXinPort;
import com.dasi.mcp.dto.NoticeWeComToolRequest;
import com.dasi.mcp.dto.NoticeWeComToolResponse;
import com.dasi.sse.dto.AccessTokenWeComResponse;
import com.dasi.sse.dto.NoticeWeComHttpRequest;
import com.dasi.sse.dto.NoticeWeComHttpResponse;
import com.dasi.sse.gateway.INoticeWeComHttp;
import com.dasi.type.properties.NoticeWeComProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

@Slf4j
@Service
public class NoticeWeComPort implements INoticeWeiXinPort {

    @Resource
    private NoticeWeComProperties noticeWeComProperties;

    @Resource
    private INoticeWeComHttp noticeWeComHttp;

    private String getAccessToken() throws IOException {

        Call<AccessTokenWeComResponse> call = noticeWeComHttp.getAccessToken(
                noticeWeComProperties.getCorpid(),
                noticeWeComProperties.getCorpsecret()
        );

        Response<AccessTokenWeComResponse> callResponse = call.execute();

        if (!callResponse.isSuccessful()) {
            String err = callResponse.errorBody() == null ? "<empty>" : callResponse.errorBody().string();
            log.error("WeCom 获取 access_token 失败: {}", err);
            return null;
        }

        AccessTokenWeComResponse tokenResponse = callResponse.body();

        if (tokenResponse == null) {
            log.error("WeCom 获取 access_token 的响应体为空");
            return null;
        }

        if (tokenResponse.getErrcode() != null && tokenResponse.getErrcode() != 0) {
            log.error("WeCom 获取 access_token 失败: {}", tokenResponse.getErrmsg());
            return null;
        }

        String accessToken = tokenResponse.getAccess_token();
        if (accessToken == null || accessToken.isBlank()) {
            log.error("WeCom 获取 access_token 的内容为空");
            return null;
        }

        log.info("调用 HTTP 获取企业微信令牌：token={}", accessToken);

        return accessToken;
    }

    @Override
    public NoticeWeComToolResponse noticeArticle(NoticeWeComToolRequest toolRequest) throws IOException {

        NoticeWeComHttpRequest.MsgData msgData = new NoticeWeComHttpRequest.MsgData();
        msgData.setTitle(toolRequest.getTitle());
        msgData.setDescription(toolRequest.getDescription());
        msgData.setUrl(toolRequest.getUrl());

        NoticeWeComHttpRequest httpRequest = new NoticeWeComHttpRequest();
        httpRequest.setAgentid(noticeWeComProperties.getAgentid());
        httpRequest.setTextcard(msgData);

        String accessToken = getAccessToken();

        NoticeWeComToolResponse toolResponse = new NoticeWeComToolResponse();

        if (accessToken == null || accessToken.isBlank()) {
            toolResponse.setCode(500);
            toolResponse.setInfo("WeCom access_token 获取失败");
            return toolResponse;
        }

        Call<NoticeWeComHttpResponse> call = noticeWeComHttp.noticeArticle(httpRequest, accessToken);
        Response<NoticeWeComHttpResponse> callResponse = call.execute();
        log.info("调用 HTTP 进行企业微信应用消息通知：标题={} 概述={} 链接={}", toolRequest.getTitle(), toolRequest.getDescription(), toolRequest.getUrl());

        if (!callResponse.isSuccessful()) {
            String err = callResponse.errorBody() == null ? "<empty>" : callResponse.errorBody().string();
            toolResponse.setCode(callResponse.code());
            toolResponse.setInfo("WeCom HTTP 请求失败: " + err);
            return toolResponse;
        }

        NoticeWeComHttpResponse httpResponse = callResponse.body();

        if (httpResponse == null) {
            toolResponse.setCode(500);
            toolResponse.setInfo("WeCom HTTP 响应体为空");
            return toolResponse;
        }

        toolResponse.setCode(httpResponse.getErrcode());
        toolResponse.setInfo(httpResponse.getErrmsg());
        toolResponse.setMsgid(httpResponse.getMsgid());

        return toolResponse;
    }

}
