package com.flashlife.repository;

import com.flashlife.entity.PostLike;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository
        extends JpaRepository<PostLike, Long> {
    /*
     * 判断：某用户是否已经点赞某帖子。
     */
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    /*
     * 查询某帖子持久化点赞数。
     */
    long countByPostId(Long postId);
    /*
     * 删除某用户对某帖子的点赞。
     */
    long deleteByPostIdAndUserId(Long postId, Long userId);
}