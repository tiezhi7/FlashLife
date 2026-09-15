package com.flashlife.service;

import com.flashlife.dto.LoginRequest;
import com.flashlife.dto.LogoutRequest;
import com.flashlife.dto.RefreshTokenRequest;
import com.flashlife.dto.RegisterRequest;
import com.flashlife.dto.TokenPairResponse;
import com.flashlife.dto.UserResponse;

import com.flashlife.entity.RefreshToken;
import com.flashlife.entity.User;

import com.flashlife.exception.BusinessException;
import com.flashlife.exception.ErrorCode;

import com.flashlife.repository.UserRepository;

import com.flashlife.security.JwtService;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
/*
 * AuthService
 * 用户认证相关业务逻辑。
 * 负责：
 * 1. 注册
 * 2. 登录
 * 3. Refresh Token
 * 4. Logout
 */
@Service
public class AuthService {
    /*
     * User 数据库访问层。
     */
    private final UserRepository userRepository;
    /*
     * BCrypt 密码组件。
     */
    private final PasswordEncoder passwordEncoder;
    /*
     * Access Token / JWT 服务。
     */
    private final JwtService jwtService;
    /*
     * Refresh Token 服务。
     */
    private final RefreshTokenService refreshTokenService;
    /*
     * 构造器注入。
     */
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository =
                userRepository;
        this.passwordEncoder =
                passwordEncoder;
        this.jwtService =
                jwtService;
        this.refreshTokenService =
                refreshTokenService;
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
    public TokenPairResponse login(
            LoginRequest request
    ) {
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
         * 创建短期 Access Token。
         */
        String accessToken =
                jwtService.generateAccessToken(
                        user
                );
        /*
         * 创建长期 Refresh Token。
         */
        String refreshToken =
                refreshTokenService.issue(
                        user.getId()
                );
        return new TokenPairResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getExpiresInSeconds(),
                refreshTokenService.getExpiresInSeconds(),
                UserResponse.from(user)
        );
    }
    /*
     * ========================================
     * 刷新登录状态
     * ========================================
     *
     * @Transactional：
     *
     * 整个刷新过程放在一个数据库事务中。
     *
     * 旧 Token：
     * 撤销
     *
     * 新 Token：
     * 创建
     *
     * 要么一起成功，
     * 要么一起失败。
     */
    @Transactional
    public TokenPairResponse refresh(
            RefreshTokenRequest request
    ) {
        /*
         * 查询旧 Refresh Token，并对数据库记录加锁。
         */
        RefreshToken oldRefreshToken =
                refreshTokenService
                        .getValidTokenForUpdate(
                                request.getRefreshToken()
                        );
        /*
         * 找到 Refresh Token 所属用户。
         */
        User user =
                userRepository
                        .findById(
                                oldRefreshToken.getUserId()
                        )
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                ErrorCode.INVALID_REFRESH_TOKEN
                                        )
                        );
        /*
         * Rotation：旧 Refresh Token 立即作废。
         */
        refreshTokenService.revoke(
                oldRefreshToken
        );
        /*
         * 创建新的 Refresh Token。
         */
        String newRefreshToken =
                refreshTokenService.issue(
                        user.getId()
                );
        /*
         * 同时签发新的 Access Token。
         */
        String newAccessToken =
                jwtService.generateAccessToken(
                        user
                );
        return new TokenPairResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                jwtService.getExpiresInSeconds(),
                refreshTokenService.getExpiresInSeconds(),
                UserResponse.from(user)
        );
    }
    /*
     * ========================================
     * 退出登录
     * ========================================
     */
    @Transactional
    public void logout(
            LogoutRequest request
    ) {
        /*
         * 找到当前 Refresh Token。
         * 找不到 / 已经失效：统一认为凭证无效。
         */
        RefreshToken refreshToken =
                refreshTokenService
                        .getValidTokenForUpdate(
                                request.getRefreshToken()
                        );
        /*
         * 撤销 Refresh Token。
         */
        refreshTokenService.revoke(
                refreshToken
        );
    }
}
