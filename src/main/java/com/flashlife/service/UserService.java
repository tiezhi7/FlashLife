package com.flashlife.service;

import com.flashlife.dto.UserResponse;

import com.flashlife.entity.User;

import com.flashlife.exception.BusinessException;
import com.flashlife.exception.ErrorCode;

import com.flashlife.repository.UserRepository;

import org.springframework.stereotype.Service;


import java.util.List;
/*
 * 用户业务逻辑。
 * Day5：注册/登录已经移动到 AuthService。
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }
    /*
     * 查询全部用户。
     */
    public List<UserResponse> getAllUsers() {
        /*
         * 先查询： List<User>
         */
        return userRepository
                .findAll()
                .stream()
                /*
                 * 每个 User转换成 UserResponse。
                 * 防止 passwordHash 泄露。
                 */
                .map(
                        UserResponse::from
                )
                /*
                 * 最终收集为：List<UserResponse>
                 */
                .toList();
    }
    /*
     * 用户数量。
     */
    public long getUserCount() {
        return userRepository.count();
    }
    /*
     * 根据 ID 查询用户。
     */
    public UserResponse getUserById(
            Long id
    ) {
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                ErrorCode.USER_NOT_FOUND
                                        )
                        );
        return UserResponse.from(
                user
        );
    }
}