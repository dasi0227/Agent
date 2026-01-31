package com.dasi.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.dasi.domain.login.model.AuthContext;
import com.dasi.domain.login.model.LoginUser;
import com.dasi.domain.login.service.JwtService;
import com.dasi.types.dto.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Set<String> WHITE_LIST = Set.of(
            "/api/v1/login",
            "/api/v1/register"
    );

    @Resource
    private JwtService jwtService;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        if (isWhiteList(uri)) {
            return true;
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(response, "未登录或登录已过期");
        }
        String token = authHeader.substring(7);
        LoginUser loginUser;
        try {
            loginUser = jwtService.parseLoginUser(token);
        } catch (JWTVerificationException e) {
            log.warn("Token 校验失败：{}", e.getMessage());
            return unauthorized(response, "登录状态无效，请重新登录");
        } catch (Exception e) {
            log.warn("Token 解析异常：{}", e.getMessage());
            return unauthorized(response, "登录状态无效，请重新登录");
        }
        AuthContext.set(loginUser);

        if (uri.startsWith("/api/v1/admin") && (loginUser.getRole() == null || !"admin".equalsIgnoreCase(loginUser.getRole()))) {
            return forbidden(response, "无权限访问该资源");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private boolean isWhiteList(String uri) {
        if (uri == null) {
            return false;
        }
        if (WHITE_LIST.stream().anyMatch(uri::startsWith)) {
            return true;
        }
        // 放行 Spring 默认错误页与静态资源
        return "/error".equals(uri) || uri.contains("favicon.ico");
    }

    private boolean unauthorized(HttpServletResponse response, String message) throws IOException {
        Result<Void> body = Result.<Void>builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .info(message)
                .build();
        writeJson(response, HttpStatus.UNAUTHORIZED.value(), body);
        return false;
    }

    private boolean forbidden(HttpServletResponse response, String message) throws IOException {
        Result<Void> body = Result.<Void>builder()
                .code(HttpStatus.FORBIDDEN.value())
                .info(message)
                .build();
        writeJson(response, HttpStatus.FORBIDDEN.value(), body);
        return false;
    }

    private void writeJson(HttpServletResponse response, int status, Result<Void> body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
