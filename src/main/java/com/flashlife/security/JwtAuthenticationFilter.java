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
    private final AccessTokenBlacklistService
            accessTokenBlacklistService;
    public JwtAuthenticationFilter(
            JwtService jwtService,
            AccessTokenBlacklistService
                    accessTokenBlacklistService
    ) {
        this.jwtService = jwtService;
        this.accessTokenBlacklistService = accessTokenBlacklistService;
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
             * 先验证 JWT，并读取 jti。
             */
            String jti = jwtService.extractJti(token);
            /*
             * ========================================
             * Redis 黑名单检查
             * ========================================
             * 如果： jti 已经被写入 Redis
             * 说明： 这枚 Access Token 已经 Logout。
             */
            if (
                    accessTokenBlacklistService
                            .isBlacklisted(jti)
            ) {
                /*
                 * 不建立认证信息。
                 */
                SecurityContextHolder
                        .clearContext();
                /*
                 * 继续进入 Spring Security 后续流程。
                 * 因为目标 API 需要 authenticated()，最终会返回 401。
                 */
                filterChain.doFilter(request, response);
                return;
            }
            /*
             * Token 没被注销。
             * 再获取当前 userId。
             */
            Long userId = jwtService.extractUserId(token);
            if (
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication()
                            == null
            ) {
                UsernamePasswordAuthenticationToken
                        authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.emptyList()
                        );
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
            SecurityContextHolder
                    .clearContext();
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
