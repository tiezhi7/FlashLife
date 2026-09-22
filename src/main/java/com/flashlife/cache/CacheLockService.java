package com.flashlife.cache;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.data.redis.core.script.DefaultRedisScript;

import org.springframework.stereotype.Service;

import java.time.Duration;

import java.util.Collections;

import java.util.UUID;

/*
 * CacheLockService
 * 作用： 使用 Redis 实现缓存重建互斥锁。 当热门 Cache Key 过期后，防止大量请求同时访问 MySQL。
 */
@Service
public class CacheLockService {
    private static final String LOCK_KEY_PREFIX = "flashlife:lock:user-cache:";
    /*
     * Lua 解锁脚本
     * Redis 中：
     * KEYS[1] =当前锁 Key
     * ARGV[1] = 当前请求的 owner
     * 只有：Redis 中保存的 owner == 当前请求 owner
     * 才允许删除这个锁。
     */
    private static final DefaultRedisScript<Long>
            UNLOCK_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    if redis.call('get', KEYS[1]) == ARGV[1] then
                        return redis.call('del', KEYS[1])
                    else
                        return 0
                    end
                    """,
                    Long.class
            );
    /*
     * Spring Data Redis 操作工具。
     */
    private final StringRedisTemplate stringRedisTemplate;
    /*
     * Redis Lock 最大生存时间。
     */
    @Value("${app.cache.user.lock-ttl-seconds}")
    private long lockTtlSeconds;
    /*
     * 构造器注入
     * CacheLockService 只需要 Redis。
     */
    public CacheLockService(
            StringRedisTemplate stringRedisTemplate
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    /*
     * 创建当前请求自己的 Lock Owner
     * 例如： 8bf71b59-...
     * 不同请求的 owner 不一样。
     */
    public String createOwner() {
        return UUID
                .randomUUID()
                .toString();
    }
    public boolean tryLock(
            Long userId,
            String owner
    ) {
        String key =
                buildLockKey(userId);
        Boolean success =
                stringRedisTemplate
                        .opsForValue()
                        .setIfAbsent(
                                key,
                                owner,
                                Duration.ofSeconds(
                                        lockTtlSeconds
                                )
                        );
        /*
         * Boolean.TRUE.equals()
         * 可以避免 success == null 时出现 NullPointerException。
         */
        return Boolean.TRUE.equals(
                success
        );
    }
    /*
     * 安全释放 Redis Lock
     */
    public void unlock(
            Long userId,
            String owner
    ) {
        String key = buildLockKey(userId);
        try {
            /*
             * Lua：
             * 比较 owner
             * +
             * 删除 Lock 在 Redis 内一次完成。
             */
            stringRedisTemplate.execute(
                    UNLOCK_SCRIPT,
                    Collections.singletonList(
                            key
                    ),
                    owner
            );
        } catch (Exception ignored) {
            /*
             * 当前学习版这里不继续抛异常。
             * 即使 unlock 暂时失败，
             * Redis Lock 自己也有 TTL，
             * 最终会自动过期。
             */
        }
    }
    /*
     * 创建 Redis Lock Key
     */
    private String buildLockKey(
            Long userId
    ) {
        return LOCK_KEY_PREFIX + userId;
    }
}
