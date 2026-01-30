package com.dasi.types.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatClientResponse {

    private String clientId;

    private String modelName;

    private String clientDesc;

}
