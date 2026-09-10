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

import com.flashlife.dto.LoginResponse;
import com.flashlife.security.JwtService;

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
    private final JwtService jwtService;
    /*
     * 构造器注入。
     * Spring 自动提供：UserRepository PasswordEncoder
     */
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
    public LoginResponse login(
            LoginRequest request
    ) {
        /*
         * 根据 username 查询用户。
         * 用户不存在：
         * 统一返回用户名或密码错误。
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
         * 验证明文密码
         * 和数据库 BCrypt Hash。
         */
        boolean passwordCorrect =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPasswordHash()
                );
        if (!passwordCorrect) {
            throw new BusinessException(
                    ErrorCode.INVALID_CREDENTIALS
            );
        }
        /*
         * 到这里说明：用户存在+密码正确。
         * 开始签发 Access Token。
         */
        String accessToken =
                jwtService.generateAccessToken(
                        user
                );
        /*
         * 返回：Access Token+Token 类型+过期时间+当前用户
         */
        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtService.getExpiresInSeconds(),
                UserResponse.from(user)
        );
    }
}
