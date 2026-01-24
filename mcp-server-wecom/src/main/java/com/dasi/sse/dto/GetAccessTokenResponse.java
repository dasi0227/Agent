package com.dasi.sse.dto;

import lombok.Data;

@Data
public class GetAccessTokenResponse {

    private Integer errcode;

    private String errmsg;

    private String access_token;

    private Integer expires_in;

}
