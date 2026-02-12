package com.dasi.trigger.handler;

import com.dasi.types.dto.result.Result;
import com.dasi.types.exception.DependencyConflictException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsable(AsyncRequestNotUsableException e) {
        // ignore，暂不处理 SSE 的莫名错误
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(DependencyConflictException.class)
    public Result<Void> handleConflict(DependencyConflictException e) {
        log.warn("依赖冲突: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleMethodNotSupported(HttpServletRequest request, HttpRequestMethodNotSupportedException e) {
        if (isFrontendRequest(request)) {
            log.warn(
                    "请求方法不支持: method={}, uri={}, query={}, referer={}, userAgent={}, remoteIp={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getQueryString(),
                    request.getHeader("Referer"),
                    request.getHeader("User-Agent"),
                    request.getRemoteAddr()
            );
        }
        return Result.<Void>builder()
                .code(HttpStatus.METHOD_NOT_ALLOWED.value())
                .info("请求方法不支持")
                .data(null)
                .build();
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error(e.getMessage(), e);
        return Result.error("服务出错，请联系管理员处理");
    }

    private boolean isFrontendRequest(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return false;
        }
        return referer.contains("/agent/")
                || referer.contains("/chat")
                || referer.contains("/work")
                || referer.contains("/admin");
    }
}
