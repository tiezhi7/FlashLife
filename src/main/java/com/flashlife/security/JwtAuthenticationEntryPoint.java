package com.flashlife.security;

import com.flashlife.common.Result;
import com.flashlife.exception.ErrorCode;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
/*
 * JwtAuthenticationEntryPoint
 * 当用户：没有登录
 * Token 不合法
 * Token 已过期
 * 却访问需要认证的 API 时，统一从这里返回 HTTP 401。
 */
@Component
public class JwtAuthenticationEntryPoint
        implements AuthenticationEntryPoint {
    /*
     * Spring Boot 4 默认配置的ackson 3 JsonMapper。
     */
    private final JsonMapper jsonMapper;
    public JwtAuthenticationEntryPoint(
            JsonMapper jsonMapper
    ) {
        this.jsonMapper = jsonMapper;
    }
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        /*
         * HTTP 状态：401 Unauthorized
         */
        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );
        /*
         * 告诉客户端：Body 是 JSON。
         */
        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );
        /*
         * 防止中文乱码。
         */
        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );
        /*
         * 继续使用项目统一 Result。
         */
        Result<Void> result =
                Result.error(
                        ErrorCode.UNAUTHORIZED.getCode(),
                        ErrorCode.UNAUTHORIZED.getMessage()
                );
        /*
         * Java Result
         * ↓
         * JSON
         */
        jsonMapper.writeValue(
                response.getWriter(),
                result
        );
    }
}
