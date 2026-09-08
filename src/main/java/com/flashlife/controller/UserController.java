package com.flashlife.controller;

import com.flashlife.common.Result;

import com.flashlife.dto.UserResponse;

import com.flashlife.service.UserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}