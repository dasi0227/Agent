package com.dasi.sse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BochaWebSearchHttpRequest {

    private String query;

    private String freshness;

    @Builder.Default
    private Boolean summary = true;

    @Builder.Default
    private String include = "";

    @Builder.Default
    private String exclude = "";

    @Builder.Default
    private Integer count = 5;

}
