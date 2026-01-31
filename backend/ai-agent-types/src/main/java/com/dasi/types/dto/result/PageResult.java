package com.dasi.types.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private Integer pageNum;

    private Integer pageSum;

    private Integer pageSize;

    private List<T> list;

}
