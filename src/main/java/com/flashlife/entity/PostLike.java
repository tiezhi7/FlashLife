package com.flashlife.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
/*
 * PostLike
 * 对应：post_likes
 */
@Entity
@Table(name = "post_likes")
public class PostLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /*
     * 被点赞的帖子 ID。
     */
    @Column(name = "post_id", nullable = false)
    private Long postId;
    /*
     * 点赞用户 ID。
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    /*
     * 点赞时间。
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    public PostLike() {
    }
    public PostLike(
            Long postId,
            Long userId,
            Instant createdAt
    ) {
        this.postId = postId;
        this.userId = userId;
        this.createdAt = createdAt;
    }
    public Long getId() {
        return id;
    }
    public Long getPostId() {
        return postId;
    }
    public Long getUserId() {
        return userId;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}
