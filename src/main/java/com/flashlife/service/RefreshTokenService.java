package com.flashlife.service;

import com.flashlife.entity.RefreshToken;

import com.flashlife.exception.BusinessException;
import com.flashlife.exception.ErrorCode;

import com.flashlife.repository.RefreshTokenRepository;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import java.util.Base64;
import java.util.HexFormat;
/*
 * RefreshTokenService
 * 专门负责：
 * 1. 创建 Refresh Token
 * 2. Hash Refresh Token
 * 3. 保存数据库
 * 4. 校验 Refresh Token
 * 5. 撤销 Refresh Token
 */
@Service
public class RefreshTokenService {
    private final RefreshTokenRepository
            refreshTokenRepository;
    /*
     * SecureRandom：用于生成安全的随机 Token。
     */
    private final SecureRandom secureRandom =
            new SecureRandom();
    /*
     * Refresh Token 生命周期。
     * 默认： 7 天。
     */
    @Value("${auth.refresh-token.expire-days}")
    private long expireDays;
    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.refreshTokenRepository =
                refreshTokenRepository;
    }
    /*
     * ========================================
     * 创建真正的随机 Refresh Token
     * ========================================
     */
    private String generateRawToken() {
        /*
         * 32 byte = 256 bit 随机数据。
         */
        byte[] randomBytes =
                new byte[32];
        secureRandom.nextBytes(
                randomBytes
        );
        /*
         * 转成 URL-safe Base64。
         * withoutPadding()
         * 去掉末尾的 =
         */
        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        randomBytes
                );
    }
    /*
     * ========================================
     * SHA-256
     * ========================================
     */
    public String hashToken(
            String rawToken
    ) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );
            byte[] hashBytes =
                    digest.digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );
            /*
             * 转成十六进制字符串。
             * 长度： 64。
             */
            return HexFormat
                    .of()
                    .formatHex(
                            hashBytes
                    );
        } catch (
                NoSuchAlgorithmException e
        ) {
            /*
             * Java 21 一定支持 SHA-256。
             * 如果真的不存在， 属于服务器环境级严重错误。
             */
            throw new IllegalStateException(
                    "SHA-256不可用",
                    e
            );
        }
    }
    /*
     * ========================================
     * 给用户签发 Refresh Token
     * ========================================
     */
    public String issue(
            Long userId
    ) {
        /*
         * 真正返回客户端的 Token。
         */
        String rawToken =
                generateRawToken();
        /*
         * 数据库保存 Hash。
         */
        String tokenHash =
                hashToken(
                        rawToken
                );
        Instant now =
                Instant.now();
        Instant expiresAt =
                now.plus(
                        expireDays,
                        ChronoUnit.DAYS
                );
        RefreshToken refreshToken =
                new RefreshToken(
                        userId,
                        tokenHash,
                        expiresAt,
                        false,
                        now
                );
        refreshTokenRepository.save(
                refreshToken
        );
        /*
         * 返回给客户端的是：原始 Token。
         */
        return rawToken;
    }
    /*
     * ========================================
     * 在刷新流程中：查询并锁住旧 Refresh Token
     * ========================================
     */
    public RefreshToken getValidTokenForUpdate(
            String rawToken
    ) {
        String tokenHash =
                hashToken(
                        rawToken
                );
        RefreshToken token =
                refreshTokenRepository
                        .findByTokenHashForUpdate(
                                tokenHash
                        )
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                ErrorCode.INVALID_REFRESH_TOKEN
                                        )
                        );
        /*
         * 已经被注销 / 使用过。
         */
        if (token.isRevoked()) {

            throw new BusinessException(
                    ErrorCode.INVALID_REFRESH_TOKEN
            );
        }
        /*
         * Token 已经过期。
         */
        if (
                Instant.now()
                        .isAfter(
                                token.getExpiresAt()
                        )
        ) {
            throw new BusinessException(
                    ErrorCode.INVALID_REFRESH_TOKEN
            );
        }
        return token;
    }
    /*
     * ========================================
     * 撤销 Token
     * ========================================
     */
    public void revoke(
            RefreshToken token
    ) {
        token.setRevoked(
                true
        );
        refreshTokenRepository.save(
                token
        );
    }
    /*
     * Refresh Token 有效秒数。
     */
    public long getExpiresInSeconds() {

        return expireDays
                * 24
                * 60
                * 60;
    }
}
