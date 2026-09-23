package com.flashlife.dto;

import com.flashlife.entity.Post;

import java.time.Instant;
/*
 * 返回客户端的帖子信息。
 * 不直接暴露 Entity。
 */
public class PostResponse {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Instant createdAt;
    public PostResponse() {
    }
    public PostResponse(
            Long id,
            Long userId,
            String title,
            String content,
            Instant createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }
    public static PostResponse from(
            Post post
    ) {
        return new PostResponse(
                post.getId(),
                post.getUserId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt()
        );
    }
    public Long getId() {
        return id;
    }
    public Long getUserId() {
        return userId;
    }
    public String getTitle() {
        return title;
    }
    public String getContent() {
        return content;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}
