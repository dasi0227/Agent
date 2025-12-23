package com.dasi.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class Result<T> implements Serializable {

    private static final String SUCCESS_CODE = "200";
    private static final String SUCCESS_INFO = "执行成功";
    private static final String ERROR_CODE = "500";
    private static final String ERROR_INFO = "执行失败";

    private String code;
    private String info;
    private T data;

    public static <T> Result<T> success() {
        return build(null, SUCCESS_CODE, SUCCESS_INFO);
    }

    public static <T> Result<T> success(T data) {
        return build(data, SUCCESS_CODE, SUCCESS_INFO);
    }

    public static <T> Result<T> success(String info, T data) {
        return build(data, SUCCESS_CODE, info);
    }

    public static <T> Result<T> error() {
        return build(null, ERROR_CODE, ERROR_INFO);
    }

    public static <T> Result<T> error(String info) {
        return build(null, ERROR_CODE, info);
    }

    private static <T> Result<T> build(T data, String code, String info) {
        return Result.<T>builder()
                .data(data)
                .code(code)
                .info(info)
                .build();
    }

}
