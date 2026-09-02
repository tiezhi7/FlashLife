package com.flashlife.controller;
import com.flashlife.common.Result;
import com.flashlife.dto.UserCreateRequest;
import com.flashlife.entity.User;
import com.flashlife.service.UserService;

import org.springframework.web.bind.annotation.GetMapping;
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
    public Result<Integer> getUserCount() {
        int count = userService.getUserCount();
        return Result.success(count);
    }
}