package com.dasi.domain.admin.model.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientQuery {

    private String keyword;

    private String clientType;

    private String modelId;

    private Integer status;

    private Integer page;

    private Integer size;
}
