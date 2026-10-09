package com.flashlife.repository;


import com.flashlife.entity.PostLike;


import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;


/*
 * PostLike 数据访问层。
 */
public interface PostLikeRepository
        extends JpaRepository<PostLike, Long> {


    boolean existsByPostIdAndUserId(
            Long postId,
            Long userId
    );


    long countByPostId(
            Long postId
    );


    long deleteByPostIdAndUserId(
            Long postId,
            Long userId
    );


    /*
     * ========================================
     * 幂等插入点赞
     * ========================================
     *
     * MySQL：
     *
     * INSERT IGNORE
     *
     * 配合：
     *
     * UNIQUE(post_id, user_id)
     *
     * 如果同一个点赞 Event
     * 被重复消费：
     *
     * 第一次：
     * INSERT 成功
     *
     * 第二次：
     * UNIQUE 冲突
     *
     * INSERT IGNORE
     * 不会插入第二条。
     */
    @Modifying
    @Query(
            value = """
                    INSERT IGNORE INTO post_likes (
                        post_id,
                        user_id,
                        created_at
                    )
                    VALUES (
                        :postId,
                        :userId,
                        CURRENT_TIMESTAMP(6)
                    )
                    """,
            nativeQuery = true
    )
    int insertIgnoreLike(
            @Param("postId")
            Long postId,

            @Param("userId")
            Long userId
    );
}