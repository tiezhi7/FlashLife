package com.flashlife.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
/*
 * Post
 * 对应数据库：posts
 */
@Entity
@Table(name = "posts")
public class Post {
    /*
     * 帖子主键。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /*
     * 发帖用户 ID。
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    /*
     * 帖子标题。
     */
    @Column(nullable = false, length = 100)
    private String title;
    /*
     * 帖子正文。
     */
    @Column(nullable = false, length = 1000)
    private String content;
    /*
     * 创建时间。
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    /*
     * JPA 需要无参构造器。
     */
    public Post() {
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