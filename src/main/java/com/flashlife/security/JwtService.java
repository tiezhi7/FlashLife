package com.flashlife.security;

import com.flashlife.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.io.Decoders;

import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
/*
 * JwtService
 * 专门负责：
 * 1. 创建 JWT
 * 2. 验证 JWT
 * 3. 从 JWT 读取当前 userId
 */
@Service
public class JwtService {
    /*
     * 从 application.properties：
     * jwt.secret=${JWT_SECRET}
     * 读取 JWT Secret。
     */
    @Value("${jwt.secret}")
    private String jwtSecret;
    /*
     * Token 有效分钟数。
     */
    @Value("${jwt.expire-minutes}")
    private long expireMinutes;
    /*
     * ========================================
     * 创建签名密钥
     * ========================================
     */
    private SecretKey getSigningKey() {
        /*
         * JWT_SECRET 是 Base64 字符串。先把 Base64解码成真正的 byte[]。
         */
        byte[] keyBytes =
                Decoders.BASE64.decode(
                        jwtSecret
                );
        /*
         * 把 byte[]转换成适合 HMAC 的 SecretKey。
         */
        return Keys.hmacShaKeyFor(
                keyBytes
        );
    }
    /*
     * ========================================
     * 生成 Access Token
     * ========================================
     */
    public String generateAccessToken(
            User user
    ) {
        /*
         * 当前时间。
         */
        Instant now =
                Instant.now();
        /*
         * 过期时间：当前时间 + expireMinutes。
         */
        Instant expiration =
                now.plus(
                        expireMinutes,
                        ChronoUnit.MINUTES
                );
        /*
         * 创建 JWT。
         */
        return Jwts.builder()
                /*
                 * iss：
                 * 谁签发 Token。
                 */
                .issuer("flashlife")
                /*
                 * sub：Subject。
                 * 我们只保存 userId。
                 */
                .subject(
                        String.valueOf(
                                user.getId()
                        )
                )
                /*
                 * iat：Token 签发时间。
                 */
                .issuedAt(
                        Date.from(now)
                )
                /*
                 * exp： Token 过期时间。
                 */
                .expiration(
                        Date.from(expiration)
                )
                /*
                 * 使用： JWT_SECRET + HS256 对 Token 签名。
                 */
                .signWith(
                        getSigningKey(),
                        Jwts.SIG.HS256
                )
                /*
                 * 最终生成：xxx.yyy.zzz
                 */
                .compact();
    }
    /*
     * ========================================
     * 解析 Token
     * ========================================
     */
    private Claims parseClaims(
            String token
    ) {
        /*
         * verifyWith()
         * 使用同一个 SecretKey验证 JWT 的 Signature。
         * parseSignedClaims()
         * 解析已经签名的 JWT Claims。
         */
        return Jwts.parser()
                .verifyWith(
                        getSigningKey()
                )
                .build()
                .parseSignedClaims(
                        token
                )
                .getPayload();
    }
    /*
     * ========================================
     * 从 Token 中获取 userId
     * ========================================
     */
    public Long extractUserId(
            String token
    ) {
        /*
         * 获取 Claims。
         */
        Claims claims =
                parseClaims(token);
        /*
         * subject 中保存的是：String userId
         * 例如： "1"
         */
        String subject =
                claims.getSubject();
        /*
         * 转换："1"
         * ↓
         * 1L
         */
        return Long.valueOf(
                subject
        );
    }
     /*
     * 返回 Token 有效秒数。
     * 登录响应会使用。
     */
    public long getExpiresInSeconds() {
        return expireMinutes * 60;
    }
}