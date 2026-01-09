package com.dasi.sse.gateway;

import com.dasi.sse.dto.AccessTokenWeComResponse;
import com.dasi.sse.dto.NoticeWeComHttpRequest;
import com.dasi.sse.dto.NoticeWeComHttpResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface INoticeWeComHttp {
    @GET("/cgi-bin/gettoken")
    Call<AccessTokenWeComResponse> getAccessToken(
            @Query("corpid") String corpId,
            @Query("corpsecret") String corpSecret);

    @POST("/cgi-bin/message/send")
    Call<NoticeWeComHttpResponse> noticeArticle(
            @Body NoticeWeComHttpRequest request,
            @Query("access_token") String accessToken);

}
