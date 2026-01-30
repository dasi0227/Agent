package com.dasi.types.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("unused")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private Integer code;
    private String info;
    private T data;

    public static <T> Result<T> success() {
        return build(null, 200, "成功");
    }

    public static <T> Result<T> success(T data) {
        return build(data, 200, "成功");
    }

    public static <T> Result<T> error() {
        return build(null, 500, "失败");
    }

    public static <T> Result<T> error(String info) {
        return build(null, 500, info);
    }

    private static <T> Result<T> build(T data, Integer code, String info) {
        return Result.<T>builder()
                .data(data)
                .code(code)
                .info(info)
                .build();
    }

}
