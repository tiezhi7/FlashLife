package com.flashlife.cache;

import com.flashlife.dto.UserResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import tools.jackson.databind.json.JsonMapper;

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
    /*
     * ========================================
     * 读取 Redis 原始字符串
     * ========================================
     * 有缓存：返回 JSON 或： __NULL__
     * 没缓存：返回 null
     */
    public String getRaw(
            Long userId
    ) {
        return stringRedisTemplate
                .opsForValue()
                .get(buildKey(userId));
    }
    /*
     * ========================================
     * 判断是否为空值缓存
     * ========================================
     */
    public boolean isNullMarker(
            String value
    ) {
        return NULL_MARKER.equals(value);
    }
    /*
     * ========================================
     * JSON → UserResponse
     * ========================================
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
     * ========================================
     * UserResponse → JSON → Redis
     * ========================================
     */
    public void putUser(
            UserResponse user
    ) {
        try {
            String json = jsonMapper.writeValueAsString(user);
            stringRedisTemplate
                    .opsForValue()
                    .set(
                            buildKey(
                                    user.getId()
                            ),
                            json,
                            Duration.ofMinutes(userTtlMinutes)
                    );
        } catch (Exception e) {
            /*
             * 这里暂时不让缓存失败阻止数据库正常查询结果返回。
             * 原因：
             * MySQL 才是 Source of Truth。
             * 缓存写失败，最坏只是下一次再查数据库。
             */
            System.out.println("写入用户缓存失败：" + e.getMessage());
        }
    }
    /*
     * ========================================
     * 写入“用户不存在”缓存
     * ========================================
     * 注意：TTL 比正常缓存短很多。
     */
    public void putNull(
            Long userId
    ) {
        stringRedisTemplate
                .opsForValue()
                .set(
                        buildKey(userId),
                        NULL_MARKER,
                        Duration.ofSeconds(
                                nullTtlSeconds
                        )
                );
    }
    /*
     * ========================================
     * 删除缓存
     * ========================================
     */
    public void evict(
            Long userId
    ) {
        stringRedisTemplate.delete(buildKey(userId));
    }
}