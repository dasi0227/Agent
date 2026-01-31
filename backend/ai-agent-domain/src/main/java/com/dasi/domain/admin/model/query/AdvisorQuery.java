package com.dasi.domain.admin.model.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvisorQuery {

    private String keyword;

    private String advisorType;

    private Integer status;

    private Integer page;

    private Integer size;
}
