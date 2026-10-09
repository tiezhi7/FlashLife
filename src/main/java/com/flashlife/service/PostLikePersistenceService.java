package com.flashlife.service;


import com.flashlife.repository.PostLikeRepository;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


/*
 * PostLikePersistenceService
 *
 * 专门负责：
 *
 * Redis Stream Event
 *          ↓
 *        MySQL
 *
 * 不负责 HTTP。
 *
 * 不负责 Redis 实时点赞。
 */
@Service
public class PostLikePersistenceService {


    private static final Logger log =
            LoggerFactory.getLogger(
                    PostLikePersistenceService.class
            );


    private final PostLikeRepository
            postLikeRepository;


    public PostLikePersistenceService(
            PostLikeRepository postLikeRepository
    ) {

        this.postLikeRepository =
                postLikeRepository;
    }


    /*
     * ========================================
     * 处理一个点赞 Event
     * ========================================
     *
     * transaction：
     *
     * MySQL 操作要么成功，
     * 要么回滚。
     */
    @Transactional
    public void persist(
            String eventType,
            Long postId,
            Long userId
    ) {


        /*
         * LIKE
         */
        if (
                "LIKE".equals(
                        eventType
                )
        ) {


            int inserted =
                    postLikeRepository
                            .insertIgnoreLike(
                                    postId,
                                    userId
                            );


            log.info(
                    "Persist LIKE event, postId={}, userId={}, inserted={}",
                    postId,
                    userId,
                    inserted
            );


            return;
        }


        /*
         * UNLIKE
         */
        if (
                "UNLIKE".equals(
                        eventType
                )
        ) {


            long deleted =
                    postLikeRepository
                            .deleteByPostIdAndUserId(
                                    postId,
                                    userId
                            );


            log.info(
                    "Persist UNLIKE event, postId={}, userId={}, deleted={}",
                    postId,
                    userId,
                    deleted
            );


            return;
        }


        /*
         * 如果 Event 类型异常：
         *
         * 不假装处理成功。
         *
         * 抛异常以后 Consumer 不会 ACK。
         */
        throw new IllegalArgumentException(
                "Unknown post-like event type: "
                        +
                        eventType
        );
    }
}