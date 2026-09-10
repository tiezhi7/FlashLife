package com.flashlife.security;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
/*
 * JwtAuthenticationFilter
 * 每次 HTTP 请求进来时：
 * 1. 查看 Authorization Header
 * 2. 有没有 Bearer Token
 * 3. 验证 JWT
 * 4. 读取 userId
 * 5. 保存到 Spring SecurityContext
 */
@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {
    private final JwtService jwtService;
    public JwtAuthenticationFilter(
            JwtService jwtService
    ) {
        this.jwtService = jwtService;
    }
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        /*
         * 从 HTTP Header 读取：Authorization
         */
        String authorizationHeader =
                request.getHeader(
                        HttpHeaders.AUTHORIZATION
                );
        /*
         * 如果根本没有 Authorization，
         * 或者不是：Bearer xxxxx那就先不认证。
         * 后面 SecurityConfig会决定该 API 是否允许匿名访问。
         */
        if (
                authorizationHeader == null
                        ||
                        !authorizationHeader.startsWith(
                                "Bearer "
                        )
        ) {
            filterChain.doFilter(
                    request,
                    response
            );
            return;
        }
        /*
         * 去掉："Bearer "
         * 前 7 个字符。
         * 剩下真正的 JWT。
         */
        String token =
                authorizationHeader.substring(7);
        try {
            /*
             * 如果：
             * Signature 错误
             * Token 过期
             * Token 格式错误
             * JJWT 会抛异常。
             * 正常情况下得到：userId。
             */
            Long userId =
                    jwtService.extractUserId(
                            token
                    );
            /*
             * 防止重复覆盖已经建立的认证信息。
             */
            if (
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication()
                            == null
            ) {
                /*
                 * 创建 Spring Security
                 * Authentication 对象。
                 * principal：我们当前使用 userId。
                 * credentials：已经验证完 JWT，不需要密码，
                 * 所以 null。
                 * authorities：Day6 暂时没有角色权限，所以空集合。
                 */
                UsernamePasswordAuthenticationToken
                        authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.emptyList()
                        );
                /*
                 * 告诉 Spring Security：当前请求已经认证。
                 * 当前用户 ID：userId
                 */
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );
            }
        } catch (
                JwtException
                |
                IllegalArgumentException e
        ) {
            /*
             * Token 有问题：不建立认证信息。
             * 后面如果目标 API
             * 需要 authenticated()，就会得到 401。
             */
            SecurityContextHolder.clearContext();
        }
        /*
         * 继续执行后续 Filter和 Controller。
         */
        filterChain.doFilter(
                request,
                response
        );
    }
}
