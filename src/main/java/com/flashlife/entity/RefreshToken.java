package com.flashlife.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
/*
 * RefreshToken Entity
 * 对应数据库：refresh_tokens
 * 注意：数据库中不会保存真正 Refresh Token。
 * 只保存： SHA-256 Hash。
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    /*
     * Refresh Token 记录主键。
     */
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;
    /*
     * 这个 Token 属于哪个用户。
     * Day7 暂时直接保存 userId。
     * 后面学习 JPA 关联关系以后，
     * 可以升级为：@ManyToOne
     */
    @Column(
            name = "user_id",
            nullable = false
    )
    private Long userId;
    /*
     * Refresh Token 的 SHA-256 Hash。
     * SHA-256 输出：32 byte
     * 转成十六进制：64 个字符。
     */
    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String tokenHash;
    /*
     * Refresh Token 过期时间。
     */
    @Column(
            name = "expires_at",
            nullable = false
    )
    private Instant expiresAt;
    /*
     * 是否已经被撤销。
     * true：不能再使用。
     */
    @Column(
            nullable = false
    )
    private boolean revoked;
    /*
     * 创建时间。
     */
    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;
    public RefreshToken() {
    }
    public RefreshToken(
            Long userId,
            String tokenHash,
            Instant expiresAt,
            boolean revoked,
            Instant createdAt
    ) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.createdAt = createdAt;
    }
    public Long getId() {
        return id;
    }
    public Long getUserId() {
        return userId;
    }
    public String getTokenHash() {
        return tokenHash;
    }
    public Instant getExpiresAt() {
        return expiresAt;
    }
    public boolean isRevoked() {
        return revoked;
    }
    public void setRevoked(
            boolean revoked
    ) {
        this.revoked = revoked;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}