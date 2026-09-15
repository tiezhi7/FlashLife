package com.flashlife.repository;

import com.flashlife.entity.RefreshToken;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.Optional;
/*
 * RefreshTokenRepository
 * Refresh Token 数据访问层。
 */
public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {
    /*
     * 普通查询。
     */
    Optional<RefreshToken> findByTokenHash(
            String tokenHash
    );
    /*
     * ========================================
     * 并发刷新专用查询
     * ========================================
     * PESSIMISTIC_WRITE：
     * 可以暂时理解为： “查询到这条 Refresh Token 后，当前事务暂时锁住它。”
     * 如果两个请求同时拿同一个，Refresh Token 去刷新，不希望两边同时成功。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
                        FROM RefreshToken r
                        WHERE r.tokenHash = :tokenHash
            """)
    Optional<RefreshToken> findByTokenHashForUpdate(
            @Param("tokenHash")
            String tokenHash
    );
}
