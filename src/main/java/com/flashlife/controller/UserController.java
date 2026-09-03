package com.flashlife.controller;
import com.flashlife.common.Result;
import com.flashlife.dto.UserCreateRequest;
import com.flashlife.entity.User;
import com.flashlife.service.UserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }
    @PostMapping
    public Result<User> createUser(
            @RequestBody UserCreateRequest request
    ){
        User user = userService.createUser(request);
        return Result.success(user);
    }
    @GetMapping
    public Result<List<User>> getAllUsers(){
        List<User> users = userService.getAllUsers();
        return Result.success(users);
    }
    /*
     * GET /api/users/count
     */
    @GetMapping("/count")
    public Result<Long> getUserCount() {
        long count = userService.getUserCount();
        return Result.success(count);
    }
    /*
     * GET /api/users/1
     * 根据 ID 查询用户。
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(
            @PathVariable Long id
    ) {
        User user = userService.getUserById(id);
        return Result.success(user);
    }
}