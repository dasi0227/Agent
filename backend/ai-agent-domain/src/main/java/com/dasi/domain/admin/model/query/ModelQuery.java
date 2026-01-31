package com.dasi.domain.admin.model.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelQuery {

    private String keyword;

    private String apiId;

    private Integer status;

    private Integer page;

    private Integer size;
}
