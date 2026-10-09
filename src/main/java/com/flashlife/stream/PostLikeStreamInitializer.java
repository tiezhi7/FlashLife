package com.flashlife.stream;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import org.springframework.dao.DataAccessException;

import org.springframework.data.redis.connection.stream.ReadOffset;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.StreamOperations;

import org.springframework.stereotype.Component;
/*
 * PostLikeStreamInitializer
 * Spring Boot 启动时：确保 Redis Stream Consumer Group 存在。
 */
@Component
public class PostLikeStreamInitializer
        implements ApplicationRunner {
    private static final Logger log =
            LoggerFactory.getLogger(
                    PostLikeStreamInitializer.class
            );
    private final StringRedisTemplate
            stringRedisTemplate;


    public PostLikeStreamInitializer(
            StringRedisTemplate stringRedisTemplate
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    /*
     * Spring Boot 启动完成阶段调用。
     */
    @Override
    public void run(
            ApplicationArguments args
    ) {
        /*
         * 明确指定：
         * Stream Field = String
         * Stream Value = String
         */
        StreamOperations<String, String, String>
                streamOperations =
                stringRedisTemplate
                        .<String, String>opsForStream();
        try {
            /*
             * 创建 Consumer Group。
             * 从 0-0 开始：
             * 意味着 Stream 已存在的历史消息
             * 也允许被处理。
             * Spring Data Redis 当前 API
             * 会在 Stream 不存在时创建 Stream。
             */
            streamOperations.createGroup(
                    PostLikeStreamNames.STREAM_KEY,
                    ReadOffset.from(
                            "0-0"
                    ),
                    PostLikeStreamNames.GROUP_NAME
            );
            log.info(
                    "Created post-like stream consumer group: {}",
                    PostLikeStreamNames.GROUP_NAME
            );
        } catch (DataAccessException e) {
            /*
             * Spring Boot 每次重启都会来到这里。
             * 如果 Group 已经存在：
             * Redis 会返回 BUSYGROUP。
             * 这是正常情况，
             * 不是程序错误。
             */
            if (
                    containsBusyGroup(
                            e
                    )
            ) {
                log.info(
                        "Post-like stream consumer group already exists: {}",
                        PostLikeStreamNames.GROUP_NAME
                );
                return;
            }
            /*
             * 其他 Redis 错误：继续抛出。
             * 不应该误认为启动正常。
             */
            throw e;
        }
    }
    /*
     * 检查异常链中是否存在 BUSYGROUP。
     */
    private boolean containsBusyGroup(
            Throwable throwable
    ) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (
                    message != null
                            &&
                            message.contains("BUSYGROUP")
            ) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
