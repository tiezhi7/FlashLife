package com.flashlife.controller;

import com.flashlife.common.Result;

import com.flashlife.dto.UserResponse;

import com.flashlife.service.UserService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flashlife.dto.UpdateNicknameRequest;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
/*
 * 用户查询 API。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(
            UserService userService
    ) {
        this.userService = userService;
    }
    /*
     * 查询所有用户。
     */
    @GetMapping
    public Result<List<UserResponse>>
    getAllUsers() {
        return Result.success(
                userService.getAllUsers()
        );
    }
    /*
     * 查询用户数量。
     */
    @GetMapping("/count")
    public Result<Long> getUserCount() {
        return Result.success(
                userService.getUserCount()
        );
    }
    /*
     * 根据 ID 查询用户。
     */
    @GetMapping("/{id}")
    public Result<UserResponse>
    getUserById(
            @PathVariable Long id
    ) {
        return Result.success(
                userService.getUserById(id)
        );
    }
    /*
     * ========================================
     * 当前登录用户
     * ========================================
     *
     * GET /api/users/me
     */
    @GetMapping("/me")
    public Result<UserResponse> getCurrentUser(
            @AuthenticationPrincipal
            Long userId
    ) {
        /*
         * userId 并不是浏览器自己传来的。
         * 它来自：JWT
         * ↓
         * JwtAuthenticationFilter
         * ↓
         * SecurityContext
         * ↓
         * @AuthenticationPrincipal
         */
        return Result.success(
                userService.getUserById(
                        userId
                )
        );
    }
    /*
     * 修改当前登录用户昵称 PATCH /api/users/me/nickname
     */
    @PatchMapping("/me/nickname")
    public Result<UserResponse> updateMyNickname(
            /*
             * 当前 userId：
             * JWT
             * ↓
             * Filter
             * ↓
             * SecurityContext
             */
            @AuthenticationPrincipal
            Long userId,
            /*
             * 请求 Body。
             */
            @Valid
            @RequestBody
            UpdateNicknameRequest request
    ) {
        UserResponse user =
                userService.updateNickname(
                        userId,
                        request.getNickname()
                );
        return Result.success(
                user
        );
    }
}