package com.flashlife.service;
import com.flashlife.dto.LoginRequest;
import com.flashlife.dto.RegisterRequest;
import com.flashlife.dto.UserResponse;

import com.flashlife.entity.User;

import com.flashlife.exception.BusinessException;
import com.flashlife.exception.ErrorCode;

import com.flashlife.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
/*
 * AuthService
 * Authentication Service
 * 专门负责：注册 登录认证 等账号认证相关业务。
 */
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    /*
     * 构造器注入。
     * Spring 自动提供：UserRepository PasswordEncoder
     */
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    /*
     * ========================================
     * 注册
     * ========================================
     */
    public UserResponse register(
            RegisterRequest request
    ) {
        /*
         * 第一道防线：Service 检查 username是否已经被注册。
         */
        if (
                userRepository.existsByUsername(
                        request.getUsername()
                )
        ) {
            throw new BusinessException(
                    ErrorCode.USERNAME_ALREADY_EXISTS
            );
        }
        /*
         * request.getPassword()
         * 这里拿到的是：用户刚刚输入的明文密码。
         */
        String rawPassword =
                request.getPassword();
        /*
         * passwordEncoder.encode()
         * 使用 BCrypt把明文密码转成密码哈希。
         */
        String passwordHash =
                passwordEncoder.encode(
                        rawPassword
                );
        /*
         * 创建 User Entity。
         *注意：User 中只保存 passwordHash。不保存 rawPassword。
         */
        User user = new User(
                request.getUsername(),
                passwordHash,
                request.getNickname()
        );
        try {
            /*
             * 保存数据库。
             */
            User savedUser =
                    userRepository.save(user);
            /*
             * 绝对不要：return savedUser;
             * 因为里面包含 passwordHash。
             * 转换成 UserResponse。
             */
            return UserResponse.from(
                    savedUser
            );
        } catch (
                DataIntegrityViolationException e
        ) {
            /*
             * 数据库 UNIQUE
             * 仍然作为最终防线。
             */
            throw new BusinessException(
                    ErrorCode.USERNAME_ALREADY_EXISTS
            );
        }
    }
    /*
     * ========================================
     * 登录认证
     * ========================================
     */
    public UserResponse login(
            LoginRequest request
    ) {
        /*
         * 根据 username 查询数据库。
         */
        User user =
                userRepository
                        .findByUsername(
                                request.getUsername()
                        )
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                ErrorCode.INVALID_CREDENTIALS
                                        )
                        );
        /*
         * passwordEncoder.matches()
         * 第一个参数：用户刚刚输入的明文密码
         * 第二个参数：数据库中的 BCrypt Hash
         */
        boolean passwordCorrect =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPasswordHash()
                );
        /*
         * 密码错误。
         */
        if (!passwordCorrect) {
            throw new BusinessException(
                    ErrorCode.INVALID_CREDENTIALS
            );
        }
        /*
         * 用户名存在
         * +
         * 密码正确
         * 当前 Day5：认证成功。
         * Day6 才产生 Token。
         */
        return UserResponse.from(
                user
        );
    }
}
