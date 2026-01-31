package com.dasi.types.dto.response.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryChatClientResponse {

    private String clientId;

    private String modelName;

    private String clientDesc;

}
