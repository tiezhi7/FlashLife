package com.flashlife.controller;

import com.flashlife.common.Result;

import com.flashlife.dto.LoginRequest;
import com.flashlife.dto.RegisterRequest;
import com.flashlife.dto.UserResponse;

import com.flashlife.service.AuthService;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flashlife.dto.LogoutRequest;
import com.flashlife.dto.RefreshTokenRequest;
import com.flashlife.dto.TokenPairResponse;
/*
 * AuthController
 * 用户认证相关 API。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(
            AuthService authService
    ) {
        this.authService = authService;
    }
    /*
     * ========================================
     * 用户注册
     * ========================================
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Result<UserResponse> register(
            @Valid
            @RequestBody
            RegisterRequest request
    ) {
        UserResponse user =
                authService.register(
                        request
                );
        return Result.success(
                user
        );
    }
    /*
     * ========================================
     * 用户登录
     * ========================================
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result<TokenPairResponse> login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {
        return Result.success(
                authService.login(
                        request
                )
        );
    }
    /*
     * ========================================
     * 刷新 Access Token
     * ========================================
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public Result<TokenPairResponse> refresh(
            @Valid
            @RequestBody
            RefreshTokenRequest request
    ) {
        return Result.success(
                authService.refresh(
                        request
                )
        );
    }
    /*
     * ========================================
     * 退出登录
     * ========================================
     */
    @PostMapping("/logout")
    public Result<Void> logout(
            /*
             * 获取 HTTP：Authorization
             * Header。
             */
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,
            @Valid
            @RequestBody
            LogoutRequest request
    ) {
        /*
         * Header： Bearer eyJ...
         * 前面的： "Bearer " 长度正好是 7。
         * substring(7)得到真正 Access Token。
         */
        String accessToken =
                authorizationHeader
                        .substring(7);
        authService.logout(request, accessToken);
        return Result.success(null);
    }
}
