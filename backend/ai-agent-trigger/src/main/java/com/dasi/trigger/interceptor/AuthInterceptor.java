package com.dasi.trigger.interceptor;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.dasi.domain.util.jwt.AuthContext;
import com.dasi.domain.auth.model.vo.UserVO;
import com.dasi.domain.util.jwt.IJwtService;
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

import static com.dasi.domain.admin.model.enumeration.UserRole.ADMIN;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Set<String> WHITE_LIST = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register"
    );

    @Resource
    private IJwtService jwtService;

    @Resource
    private AuthContext authContext;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        if (WHITE_LIST.stream().anyMatch(uri::startsWith)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(response, "未登录或登录已过期");
        }

        String token = authHeader.substring(7);
        UserVO userVO;

        try {
            userVO = jwtService.parseToken(token);
        } catch (JWTVerificationException e) {
            log.warn("Token 校验失败：{}", e.getMessage());
            return unauthorized(response, "登录状态无效，请重新登录");
        } catch (Exception e) {
            log.warn("Token 解析异常：{}", e.getMessage());
            return unauthorized(response, "登录状态无效，请重新登录");
        }

        authContext.set(userVO);

        if (uri.startsWith("/api/v1/admin") && (userVO.getRole() == null || !ADMIN.getRole().equalsIgnoreCase(userVO.getRole()))) {
            return forbidden(response, "无权限访问该资源");
        }

        log.info("【后端服务】服务接口：uri={}", uri);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        authContext.clear();
    }

    private boolean unauthorized(HttpServletResponse response, String message) throws IOException {
        Result<Void> body = Result.error(message);
        writeResponse(response, HttpStatus.UNAUTHORIZED.value(), body);
        return false;
    }

    private boolean forbidden(HttpServletResponse response, String message) throws IOException {
        Result<Void> body = Result.error(message);
        writeResponse(response, HttpStatus.FORBIDDEN.value(), body);
        return false;
    }

    private void writeResponse(HttpServletResponse response, int status, Result<Void> body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

}
