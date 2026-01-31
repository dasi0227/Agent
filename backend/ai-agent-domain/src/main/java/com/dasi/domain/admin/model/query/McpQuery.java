package com.dasi.domain.admin.model.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpQuery {

    private String keyword;

    private String mcpType;

    private Integer status;

    private Integer page;

    private Integer size;
}
