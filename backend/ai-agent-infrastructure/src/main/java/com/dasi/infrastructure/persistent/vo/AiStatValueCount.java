package com.dasi.infrastructure.persistent.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiStatValueCount {
    private String statValue;
    private Integer totalCount;
}

