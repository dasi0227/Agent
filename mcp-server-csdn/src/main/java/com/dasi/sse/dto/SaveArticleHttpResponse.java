package com.dasi.sse.dto;

import lombok.Data;

@Data
public class SaveArticleHttpResponse {

    // 接口返回状态码，200 表示成功
    private Integer code;

    // 请求链路追踪 ID，用于排查问题和日志定位
    private String traceId;

    // 文章相关数据主体
    private ArticleData data;

    // 接口返回状态信息
    private String msg;

    @Data
    public static class ArticleData {

        // 文章发布网址
        private String url;

        // 文章二维码地址
        private String qrcode;

        // 文章ID
        private Long id;

        // 文章标题
        private String title;

        // 文章摘要
        private String description;
    }

}
