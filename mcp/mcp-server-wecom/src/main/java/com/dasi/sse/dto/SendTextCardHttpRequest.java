package com.dasi.sse.dto;

import lombok.Data;

@Data
public class SendTextCardHttpRequest {

    // 发送应用
    private Integer agentid;

    // 发送内容
    private TextCard textcard;

    // 发送给所有人
    private String touser = "@all";

    // 发送类型
    private String msgtype = "textcard";

    // 是否开启id转译
    private Integer enable_id_trans = 0;

    // 是否开启重复消息检查
    private Integer enable_duplicate_check = 1;

    // 重复消息检查的时间间隔
    private Integer duplicate_check_interval = 1800;

    @Data
    public static class TextCard {

        private String title;

        private String description;

        private String url;

        private String btntxt = "点击跳转查看";

    }

}
