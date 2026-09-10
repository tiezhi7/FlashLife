package com.flashlife.controller;

import com.flashlife.common.Result;

import com.flashlife.dto.LoginRequest;
import com.flashlife.dto.RegisterRequest;
import com.flashlife.dto.UserResponse;

import com.flashlife.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flashlife.dto.LoginResponse;
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
    public Result<LoginResponse> login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {
        LoginResponse loginResponse =
                authService.login(
                        request
                );
        return Result.success(
                loginResponse
        );
    }
}
