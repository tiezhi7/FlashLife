package com.flashlife.stream;


import com.flashlife.service.PostLikePersistenceService;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.beans.factory.annotation.Value;


import org.springframework.data.redis.connection.stream.Consumer;

import org.springframework.data.redis.connection.stream.MapRecord;

import org.springframework.data.redis.connection.stream.ReadOffset;

import org.springframework.data.redis.connection.stream.StreamOffset;

import org.springframework.data.redis.connection.stream.StreamReadOptions;


import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.data.redis.core.StreamOperations;


import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Component;


import java.util.List;

import java.util.Map;


/*
 * PostLikeEventConsumer
 *
 * 后台读取 Redis Stream：
 *
 * LIKE
 * UNLIKE
 *
 * 然后异步持久化到 MySQL。
 */
@Component
public class PostLikeEventConsumer {


    private static final Logger log =
            LoggerFactory.getLogger(
                    PostLikeEventConsumer.class
            );


    private final StringRedisTemplate
            stringRedisTemplate;


    private final PostLikePersistenceService
            persistenceService;


    /*
     * 当前 Consumer 名称。
     *
     * 本地默认：
     *
     * consumer-local-1
     */
    @Value("${app.post-like.stream.consumer-name}")
    private String consumerName;


    /*
     * 一次最多读取：
     *
     * 20 条。
     */
    @Value("${app.post-like.stream.batch-size}")
    private long batchSize;


    public PostLikeEventConsumer(
            StringRedisTemplate stringRedisTemplate,
            PostLikePersistenceService persistenceService
    ) {

        this.stringRedisTemplate =
                stringRedisTemplate;

        this.persistenceService =
                persistenceService;
    }


    /*
     * ========================================
     * 后台轮询
     * ========================================
     *
     * fixedDelay：
     *
     * 上一次执行结束后，
     * 等指定时间，
     * 再开始下一次。
     */
    @Scheduled(
            fixedDelayString =
                    "${app.post-like.stream.poll-delay-ms}"
    )
    public void poll() {


        /*
         * 第一步：
         *
         * 优先处理当前 Consumer
         * 自己之前失败、尚未 ACK 的消息。
         */
        consumePendingMessages();


        /*
         * 第二步：
         *
         * 再读取新的消息。
         */
        consumeNewMessages();
    }


    /*
     * ========================================
     * 重试 Pending Messages
     * ========================================
     */
    private void consumePendingMessages() {


        readAndProcess(
                ReadOffset.from(
                        "0-0"
                )
        );
    }


    /*
     * ========================================
     * 读取从未投递的新消息
     * ========================================
     */
    private void consumeNewMessages() {


        readAndProcess(
                ReadOffset.lastConsumed()
        );
    }


    /*
     * ========================================
     * 真正读取 Redis Stream
     * ========================================
     */
    private void readAndProcess(
            ReadOffset readOffset
    ) {


        /*
         * 明确告诉 Java：
         *
         * Stream Key = String
         * Field = String
         * Value = String
         */
        StreamOperations<String, String, String>
                streamOperations =
                stringRedisTemplate
                        .<String, String>opsForStream();


        /*
         * Consumer Group 读取。
         */
        List<MapRecord<String, String, String>>
                records;


        try {


            records =
                    streamOperations.read(
                            Consumer.from(
                                    PostLikeStreamNames.GROUP_NAME,
                                    consumerName
                            ),

                            StreamReadOptions
                                    .empty()
                                    .count(
                                            batchSize
                                    ),

                            StreamOffset.create(
                                    PostLikeStreamNames.STREAM_KEY,
                                    readOffset
                            )
                    );


        } catch (Exception e) {


            log.error(
                    "Failed to read post-like Redis Stream",
                    e
            );


            return;
        }


        /*
         * Redis 没有消息。
         */
        if (
                records == null
                        ||
                        records.isEmpty()
        ) {

            return;
        }


        /*
         * 一条一条处理。
         */
        for (
                MapRecord<String, String, String>
                        record
                :
                records
        ) {


            processRecord(
                    streamOperations,
                    record
            );
        }
    }


    /*
     * ========================================
     * 处理一条 Event
     * ========================================
     */
    private void processRecord(

            StreamOperations<String, String, String>
                    streamOperations,

            MapRecord<String, String, String>
                    record
    ) {


        try {


            /*
             * Redis Stream Message 内容。
             */
            Map<String, String> value =
                    record.getValue();


            String eventId =
                    value.get(
                            "eventId"
                    );


            String eventType =
                    value.get(
                            "eventType"
                    );


            Long postId =
                    Long.valueOf(
                            value.get(
                                    "postId"
                            )
                    );


            Long userId =
                    Long.valueOf(
                            value.get(
                                    "userId"
                            )
                    );


            String occurredAt =
                    value.get(
                            "occurredAt"
                    );


            log.info(
                    "Consume post-like event, streamId={}, eventId={}, type={}, postId={}, userId={}, occurredAt={}",
                    record.getId(),
                    eventId,
                    eventType,
                    postId,
                    userId,
                    occurredAt
            );


            /*
             * ====================================
             * 写 MySQL
             * ====================================
             */
            persistenceService.persist(
                    eventType,
                    postId,
                    userId
            );


            /*
             * ====================================
             * MySQL 成功以后才 ACK
             * ====================================
             *
             * 如果上面的数据库代码抛异常，
             *
             * 程序永远不会走到这里。
             *
             * 该 Event 就会留在 Pending。
             */
            Long acknowledged =
                    streamOperations
                            .acknowledge(
                                    PostLikeStreamNames.GROUP_NAME,
                                    record
                            );


            log.info(
                    "ACK post-like event, streamId={}, acknowledged={}",
                    record.getId(),
                    acknowledged
            );


        } catch (Exception e) {


            /*
             * 非常重要：
             *
             * 这里绝对不能 ACK。
             *
             * 因为数据库没有确认成功。
             */
            log.error(
                    "Failed to persist post-like event, streamId={}, message will remain pending",
                    record.getId(),
                    e
            );
        }
    }
}