package com.flashlife.security;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
/*
 * AccessTokenBlacklistService
 * 专门负责：
 * 1. 把已注销 Access Token 的 jti 写入 Redis
 * 2. 给 Redis Key 设置 TTL
 * 3. 判断一枚 Access Token 是否已经注销
 */
@Service
public class AccessTokenBlacklistService {
    /*
     * Redis Key 前缀。
     * 最终例如：flashlife:auth:blacklist:access:abc-123
     */
    private static final String KEY_PREFIX =
            "flashlife:auth:blacklist:access:";
    /*
     * Spring Data Redis 提供的工具。
     * StringRedisTemplate：
     * 特别适合处理：
     * String Key
     * String Value
     */
    private final StringRedisTemplate
            stringRedisTemplate;
    /*
     * 用于：
     * 从 JWT 中读取 jti
     * 和 expiration。
     */
    private final JwtService jwtService;
    public AccessTokenBlacklistService(
            StringRedisTemplate stringRedisTemplate,
            JwtService jwtService
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.jwtService = jwtService;
    }
    /*
     * ========================================
     * 注销 Access Token
     * ========================================
     */
    public void blacklist(
            String accessToken
    ) {
        /*
         * 从 JWT 获取： jti
         */
        String jti =
                jwtService.extractJti(
                        accessToken
                );
        /*
         * 从 JWT 获取：exp
         */
        Instant expiration =
                jwtService.extractExpiration(
                        accessToken
                );
        /*
         * 当前时间。
         */
        Instant now =
                Instant.now();
        /*
         * Token 已经到期的话，根本没有必要写 Redis。
         */
        if (
                !expiration.isAfter(now)
        ) {
            return;
        }
        /*
         * Redis Key。
         */
        String key = KEY_PREFIX + jti;
        /*
         * 剩余生存时间：expiration - now
         */
        Duration ttl =
                Duration.between(
                        now,
                        expiration
                );
        /*
         * 写入 Redis：
         * key: flashlife:auth:blacklist:access:<jti>
         * value: 1
         * TTL: Access Token 剩余寿命
         */
        stringRedisTemplate
                .opsForValue()
                .set(
                        key,
                        "1",
                        ttl
                );
    }
    /*
     * ========================================
     * 判断 Token 是否已注销
     * ========================================
     */
    public boolean isBlacklisted(
            String jti
    ) {
        /*
         * 如果没有 jti，Day8 以后认为这是无效 Token。
         */
        if (
                jti == null
                        ||
                        jti.isBlank()
        ) {
            return true;
        }
        String key =
                KEY_PREFIX + jti;
        Boolean exists =
                stringRedisTemplate
                        .hasKey(
                                key
                        );
        /*
         * hasKey 返回 Boolean。
         * Boolean.TRUE.equals(...)可以避免 null 带来的问题。
         */
        return Boolean.TRUE.equals(
                exists
        );
    }
}