package com.dasi.types.dto.request.admin.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfigListRequest {

    private String idKeyword;

    private String valueKeyword;

    private String configType;

}