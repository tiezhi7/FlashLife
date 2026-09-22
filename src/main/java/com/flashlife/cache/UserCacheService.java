package com.flashlife.cache;

import com.flashlife.dto.UserResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import tools.jackson.databind.json.JsonMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataAccessException;

import java.util.concurrent.ThreadLocalRandom;

import java.time.Duration;
/*
 * UserCacheService专门负责： UserResponse和Redis之间的操作。
 * UserService 不需要知道Redis 的底层 JSON 细节。
 */
@Service
public class UserCacheService {
    /*
     * Redis Key 前缀。
     * 最终例如：flashlife:cache:user:1
     */
    private static final String KEY_PREFIX = "flashlife:cache:user:";
    /*
     * 特殊字符串： 表示：“这个用户不存在。”
     */
    private static final String NULL_MARKER = "__NULL__";
    private static final Logger log = LoggerFactory.getLogger(UserCacheService.class);
    /*
     * Spring Data Redis 提供。
     * 用来操作： String Key String Value
     */
    private final StringRedisTemplate stringRedisTemplate;
    /*
     * Spring Boot 4 使用的 JSON Mapper。
     * 我们会：
     * UserResponse
     *      ↓
     * JSON
     *      ↓
     * Redis
     */
    private final JsonMapper jsonMapper;
    /*
     * 正常用户缓存 TTL。单位：分钟。
     */
    @Value("${app.cache.user.ttl-minutes}")
    private long userTtlMinutes;
    /*
     * 空值缓存 TTL。单位：秒。
     */
    @Value("${app.cache.user.null-ttl-seconds}")
    private long nullTtlSeconds;
    /*
     * 正常缓存随机 TTL 最大偏移。 单位：秒。
     */
    @Value("${app.cache.user.ttl-jitter-seconds}")
    private long userTtlJitterSeconds;
    /*
     * 空值缓存随机 TTL 最大偏移。
     */
    @Value("${app.cache.user.null-ttl-jitter-seconds}")
    private long nullTtlJitterSeconds;
    public UserCacheService(
            StringRedisTemplate stringRedisTemplate,
            JsonMapper jsonMapper
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.jsonMapper = jsonMapper;
    }
    /*
     * ========================================
     * 生成 Redis Key
     * ========================================
     */
    private String buildKey(
            Long userId
    ) {
        return KEY_PREFIX + userId;
    }
    public String getRaw(
            Long userId
    ) {
        try {
            return stringRedisTemplate
                    .opsForValue()
                    .get(buildKey(userId));
        } catch (DataAccessException e) {
            /*
             * Redis 普通缓存故障：返回 null。
             * 上层看到 null，就会当作 Cache Miss然后查询 MySQL。
             */
            log.warn(
                    "User cache unavailable, degrade to database, userId={}",
                    userId,
                    e
            );
            return null;
        }
    }
    /*
     * 判断是否为空值缓存
     */
    public boolean isNullMarker(
            String value
    ) {
        return NULL_MARKER.equals(value);
    }
    /*
     * JSON → UserResponse
     */
    public UserResponse deserialize(
            Long userId,
            String json
    ) {
        try {
            return jsonMapper.readValue(
                    json,
                    UserResponse.class
            );
        } catch (Exception e) {
            /*
             * 如果 Redis 中的数据损坏，不应该让整个接口永久失败。
             * 直接删除这份坏缓存。后面让程序重新查 MySQL。
             */
            evict(userId);
            return null;
        }
    }
    /*
     * UserResponse → JSON → Redis
     */
    public void putUser(
            UserResponse user
    ) {
        try {
            String json = jsonMapper.writeValueAsString(user);
            /*
             * 每次写缓存时，
             * 都产生稍有差异的 TTL。
             */
            Duration ttl = buildUserTtl();
            stringRedisTemplate
                    .opsForValue()
                    .set(
                            buildKey(user.getId()),
                            json,
                            ttl
                    );
            log.info(
                    "User cache written, userId={}, ttlSeconds={}",
                    user.getId(),
                    ttl.toSeconds()
            );
        } catch (Exception e) {
            /*
             * Cache 不是 Source of Truth。
             * 写缓存失败：不应该阻止数据库结果返回。
             */
            log.warn(
                    "Failed to write user cache, userId={}",
                    user.getId(),
                    e
            );
        }
    }
    public void putNull(
            Long userId
    ) {
        try {
            Duration ttl = buildNullTtl();
            stringRedisTemplate
                    .opsForValue()
                    .set(
                            buildKey(userId),
                            NULL_MARKER,
                            ttl
                    );
            log.info(
                    "Null user cache written, userId={}, ttlSeconds={}",
                    userId,
                    ttl.toSeconds()
            );
        } catch (DataAccessException e) {
            /*
             * Redis 缓存故障：不影响真正数据库查询。
             */
            log.warn(
                    "Failed to write null cache, userId={}",
                    userId,
                    e
            );
        }
    }
    public void evict(
            Long userId
    ) {
        try {
            stringRedisTemplate.delete(buildKey(userId));
        } catch (DataAccessException e) {
            /*
             * 删除缓存失败时： MySQL 已经是正确数据。
             * Redis 中旧缓存最终还会因为 TTL自动消失。
             * 当前先记录警告。
             */
            log.warn(
                    "Failed to evict user cache, userId={}",
                    userId,
                    e
            );
        }
    }
    /*
     * 空值缓存 TTL
     */
    private Duration buildUserTtl() {
        long jitterSeconds = 0;
        if (
                userTtlJitterSeconds > 0
        ) {
            jitterSeconds =
                    ThreadLocalRandom
                            .current()
                            .nextLong(userTtlJitterSeconds + 1);
        }
        return Duration
                .ofMinutes(userTtlMinutes)
                .plusSeconds(jitterSeconds);
    }
    /*
     * 创建空值缓存 TTL
     * 当用户不存在时： Redis 中会保存： __NULL__
     * 空值缓存不能保存太久， 所以基础 TTL 比正常 User Cache 短。
     */
    private Duration buildNullTtl() {
        /*
         * 默认不增加随机时间。
         */
        long jitterSeconds = 0;
        if (
                nullTtlJitterSeconds > 0
        ) {
            jitterSeconds =
                    ThreadLocalRandom
                            .current()
                            .nextLong(
                                    nullTtlJitterSeconds + 1
                            );
        }
        /*
         * 最终空值缓存 TTL： 基础秒数 +随机秒数
         */
        return Duration
                .ofSeconds(nullTtlSeconds)
                .plusSeconds(jitterSeconds);
    }
}