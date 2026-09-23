package com.flashlife.service;

import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.data.redis.core.script.DefaultRedisScript;

import org.springframework.stereotype.Service;

import java.util.Arrays;
/*
 * PostLikeRedisService
 * 专门负责：Redis 中的点赞状态和点赞计数。
 */
@Service
public class PostLikeRedisService {
    /*
     * 用户点赞集合 Key。
     * 最终：flashlife:post:likes:users:1
     */
    private static final String USERS_KEY_PREFIX = "flashlife:post:likes:users:";
    /*
     * 点赞数量 Key。
     */
    private static final String COUNT_KEY_PREFIX = "flashlife:post:likes:count:";
    /*
     * 点赞 Lua
     * KEYS[1] =点赞用户集合
     * KEYS[2] =点赞数量
     * ARGV[1] =userId
     */
    private static final DefaultRedisScript<Long>
            LIKE_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local added = redis.call(
                        'SADD',
                        KEYS[1],
                        ARGV[1]
                    )

                    if added == 1 then
                        redis.call(
                            'INCR',
                            KEYS[2]
                        )
                    end

                    local count = redis.call(
                        'GET',
                        KEYS[2]
                    )

                    if not count then
                        return 0
                    end

                    return tonumber(count)
                    """,
                    Long.class
            );
    /*
     * 取消点赞 Lua
     */
    private static final DefaultRedisScript<Long>
            UNLIKE_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local removed = redis.call(
                        'SREM',
                        KEYS[1],
                        ARGV[1]
                    )

                    if removed == 1 then

                        local current = redis.call(
                            'GET',
                            KEYS[2]
                        )

                        if current then

                            current = tonumber(current)

                            if current > 0 then
                                redis.call(
                                    'DECR',
                                    KEYS[2]
                                )
                            else
                                redis.call(
                                    'SET',
                                    KEYS[2],
                                    0
                                )
                            end
                        end
                    end

                    local count = redis.call(
                        'GET',
                        KEYS[2]
                    )

                    if not count then
                        return 0
                    end

                    return tonumber(count)
                    """,
                    Long.class
            );
    private final StringRedisTemplate stringRedisTemplate;
    public PostLikeRedisService(
            StringRedisTemplate stringRedisTemplate
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    /*
     * 点赞
     */
    public long like(
            Long postId,
            Long userId
    ) {
        Long count =
                stringRedisTemplate.execute(
                        LIKE_SCRIPT,
                        Arrays.asList(
                                buildUsersKey(postId),
                                buildCountKey(postId)
                        ),
                        String.valueOf(userId)
                );
        return count == null
                ?
                0L
                :
                count;
    }
    /*
     * 取消点赞
     */
    public long unlike(
            Long postId,
            Long userId
    ) {
        Long count =
                stringRedisTemplate.execute(
                        UNLIKE_SCRIPT,
                        Arrays.asList(
                                buildUsersKey(postId),
                                buildCountKey(postId)
                        ),
                        String.valueOf(userId)
                );
        return count == null
                ?
                0L
                :
                count;
    }
    /*
     * 当前用户是否点赞
     */
    public boolean hasLiked(
            Long postId,
            Long userId
    ) {
        Boolean member =
                stringRedisTemplate
                        .opsForSet()
                        .isMember(
                                buildUsersKey(postId),
                                String.valueOf(userId)
                        );
        return Boolean.TRUE.equals(member);
    }
    /*
     * 获取点赞数
     */
    public long getLikeCount(
            Long postId
    ) {
        String value =
                stringRedisTemplate
                        .opsForValue()
                        .get(buildCountKey(postId));
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(value);
    }
    private String buildUsersKey(
            Long postId
    ) {
        return USERS_KEY_PREFIX + postId;
    }
    private String buildCountKey(
            Long postId
    ) {
        return COUNT_KEY_PREFIX + postId;
    }
}
