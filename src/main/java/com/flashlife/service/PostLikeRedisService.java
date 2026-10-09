package com.flashlife.service;


import com.flashlife.stream.PostLikeStreamNames;


import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.data.redis.core.script.DefaultRedisScript;

import org.springframework.stereotype.Service;


import java.time.Instant;

import java.util.Arrays;

import java.util.UUID;


/*
 * PostLikeRedisService
 * Day12 职责：
 * 1. Redis 实时点赞状态
 * 2. Redis 点赞计数
 * 3. Redis Stream 点赞事件
 * 最重要：
 * 状态修改
 * +
 * Counter 修改
 * +
 * Event 写入
 * 都在同一个 Lua 中完成。
 */
@Service
public class PostLikeRedisService {
    /*
     * 点赞用户 Set。
     * 例如：flashlife:post:likes:users:1
     */
    private static final String USERS_KEY_PREFIX = "flashlife:post:likes:users:";
    /*
     * 点赞数量。
     * 例如：flashlife:post:likes:count:1
     */
    private static final String COUNT_KEY_PREFIX = "flashlife:post:likes:count:";
    /*
     * LIKE Lua
     * KEYS[1]=点赞用户 Set
     * KEYS[2]=点赞 Count
     * KEYS[3]=Redis Stream
     * ARGV[1]=userId
     * ARGV[2]=postId
     * ARGV[3]=eventId
     * ARGV[4]=occurredAt
     */
    private static final DefaultRedisScript<Long>
            LIKE_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local added =
                        redis.call(
                            'SADD',
                            KEYS[1],
                            ARGV[1]
                        )
                    if added == 1 then
                        local count =
                            redis.call(
                                'INCR',
                                KEYS[2]
                            )
                        redis.call(
                            'XADD',
                            KEYS[3],
                            '*',
                            'eventId',
                            ARGV[3],
                            'eventType',
                            'LIKE',
                            'postId',
                            ARGV[2],
                            'userId',
                            ARGV[1],
                            'occurredAt',
                            ARGV[4]
                        )
                        return count
                    end
                    local count =
                        redis.call(
                            'GET',
                            KEYS[2]
                        )
                    if not count then
                        count =
                            redis.call(
                                'SCARD',
                                KEYS[1]
                            )
                        redis.call(
                            'SET',
                            KEYS[2],
                            count
                        )
                    end
                    return tonumber(count)
                    """,
                    Long.class
            );
    /*
     * UNLIKE Lua
     */
    private static final DefaultRedisScript<Long>
            UNLIKE_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local removed =
                        redis.call(
                            'SREM',
                            KEYS[1],
                            ARGV[1]
                        )
                    if removed == 1 then
                        local current =
                            redis.call(
                                'GET',
                                KEYS[2]
                            )
                        if current then
                            current =
                                tonumber(current)
                            if current > 0 then
                                current =
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
                                current = 0
                            end
                        else
                            current =
                                redis.call(
                                    'SCARD',
                                    KEYS[1]
                                )
                            redis.call(
                                'SET',
                                KEYS[2],
                                current
                            )
                        end
                        redis.call(
                            'XADD',
                            KEYS[3],
                            '*',
                            'eventId',
                            ARGV[3],
                            'eventType',
                            'UNLIKE',
                            'postId',
                            ARGV[2],
                            'userId',
                            ARGV[1],
                            'occurredAt',
                            ARGV[4]
                        )
                        return current
                    end
                    local count =
                        redis.call(
                            'GET',
                            KEYS[2]
                        )
                    if not count then
                        count =
                            redis.call(
                                'SCARD',
                                KEYS[1]
                            )
                        redis.call(
                            'SET',
                            KEYS[2],
                            count
                        )
                    end
                    return tonumber(count)
                    """,
                    Long.class
            );
    private final StringRedisTemplate
            stringRedisTemplate;
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
        /*
         * 每一次真实状态变化
         * 都有自己的 Event ID。
         */
        String eventId =
                UUID
                        .randomUUID()
                        .toString();
        String occurredAt =
                Instant
                        .now()
                        .toString();
        Long count =
                stringRedisTemplate.execute(
                        LIKE_SCRIPT,
                        Arrays.asList(
                                buildUsersKey(postId),
                                buildCountKey(postId),
                                PostLikeStreamNames.STREAM_KEY
                        ),
                        String.valueOf(userId),
                        String.valueOf(postId),
                        eventId,
                        occurredAt
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
        String eventId =
                UUID
                        .randomUUID()
                        .toString();
        String occurredAt =
                Instant
                        .now()
                        .toString();
        Long count =
                stringRedisTemplate.execute(
                        UNLIKE_SCRIPT,
                        Arrays.asList(
                                buildUsersKey(postId),
                                buildCountKey(postId),
                                PostLikeStreamNames.STREAM_KEY
                        ),
                        String.valueOf(userId),
                        String.valueOf(postId),
                        eventId,
                        occurredAt
                );
        return count == null
                ?
                0L
                :
                count;
    }
    /*
     * 当前用户有没有点赞。
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
     * 查询点赞数。
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