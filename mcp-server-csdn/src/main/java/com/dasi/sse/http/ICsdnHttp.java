package com.dasi.sse.http;

import com.dasi.sse.dto.SaveArticleHttpRequest;
import com.dasi.sse.dto.SaveArticleHttpResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ICsdnHttp {

    @Headers({
            "accept: */*",
            "accept-encoding: gzip, deflate, br, zstd",
            "accept-language: zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,en-GB;q=0.6",
            "cache-control: no-cache",
            "content-type: application/json",
            "origin: https://editor.csdn.net",
            "pragma: no-cache",
            "priority: u=1, i",
            "referer: https://editor.csdn.net/",
            "sec-ch-ua: \"Microsoft Edge\";v=\"143\", \"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\"",
            "sec-ch-ua-mobile: ?1",
            "sec-ch-ua-platform: \"Android\"",
            "sec-fetch-dest: empty",
            "sec-fetch-mode: cors",
            "sec-fetch-site: same-site",
            "user-agent: Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Mobile Safari/537.36 Edg/143.0.0.0",
            "x-ca-key: 203803574",
            "x-ca-nonce: a1f9c545-67b0-4b7e-ad52-259ef031d865",
            "x-ca-signature: GDdOEHQBkKcyNkm+gZ/cAR54pAGS7vHLZMxkaXnn3b4=",
            "x-ca-signature-headers: x-ca-key,x-ca-nonce"
    })
    @POST("blog-console-api/v3/mdeditor/saveArticle")
    Call<SaveArticleHttpResponse> saveArticle(
            @Body SaveArticleHttpRequest serviceRequest,
            @Header("Cookie") String cookie);

}
