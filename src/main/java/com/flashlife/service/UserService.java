package com.flashlife.service;

import com.flashlife.dto.UserResponse;

import com.flashlife.entity.User;

import com.flashlife.exception.BusinessException;
import com.flashlife.exception.ErrorCode;

import com.flashlife.repository.UserRepository;

import org.springframework.stereotype.Service;

import com.flashlife.cache.CacheLockService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.flashlife.cache.CacheLockService;
import com.flashlife.cache.UserCacheService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;

import java.util.List;
/*
 * 用户业务逻辑。
 * Day5：注册/登录已经移动到 AuthService。
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserCacheService userCacheService;
    private final CacheLockService cacheLockService;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    /*
     * 没抢到 Redis Lock 后，每隔多少毫秒再检查缓存。
     */
    @Value("${app.cache.user.lock-wait-millis}")
    private long lockWaitMillis;
    /*
     * 最多检查多少次。
     */
    @Value("${app.cache.user.lock-retry-times}")
    private int lockRetryTimes;
    public UserService(
            UserRepository userRepository,
            UserCacheService userCacheService,
            CacheLockService cacheLockService
    ) {
        this.userRepository = userRepository;
        this.userCacheService = userCacheService;
        this.cacheLockService = cacheLockService;
    }
    /*
     * 查询全部用户。
     */
    public List<UserResponse> getAllUsers() {
        /*
         * 先查询： List<User>
         */
        return userRepository
                .findAll()
                .stream()
                /*
                 * 每个 User转换成 UserResponse。
                 * 防止 passwordHash 泄露。
                 */
                .map(
                        UserResponse::from
                )
                /*
                 * 最终收集为：List<UserResponse>
                 */
                .toList();
    }
    /*
     * 用户数量。
     */
    public long getUserCount() {
        return userRepository.count();
    }
    /*
     * ========================================
     * 根据 ID 查询用户
     * ========================================
     */
    public UserResponse getUserById(
            Long id
    ) {
        /*
         * 第一步：查询 Redis
         */
        String cachedValue =
                userCacheService.getRaw(id);
        /*
         * cachedValue != null  代表 Redis 中存在这个 Key。
         */
        if (cachedValue != null) {
            /*
             * 情况 1： Redis 告诉我们：这个用户不存在。
             */
            if (
                    userCacheService
                            .isNullMarker(cachedValue)
            ) {
                System.out.println("[CACHE NULL HIT] userId=" + id);
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }
            /*
             * 情况 2：Redis 中有真实用户 JSON。
             */
            UserResponse cachedUser = userCacheService
                            .deserialize(id, cachedValue);
            /*
             * 反序列化成功。
             */
            if (cachedUser != null) {
                System.out.println("[CACHE HIT] userId=" + id);
                return cachedUser;
            }
            /*
             * 如果反序列化失败：UserCacheService 已经把坏缓存删除。
             * 继续向下查询 MySQL。
             */
        }
        /*
         * 第二步： Redis Cache Miss
         */
        System.out.println("[CACHE MISS] userId=" + id);
        /*
         * 查询 MySQL。
         */
        User user = userRepository
                        .findById(id)
                        .orElse(null);
        /*
         * 第三步：MySQL 也没有这个用户
         */
        if (user == null) {
            /*
             * 写一个短 TTL 的空值缓存。
             * 防止同一个不存在 ID不断攻击数据库。
             */
            userCacheService.putNull(id);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        /*
         * Entity
         * ↓
         * Response DTO
         */
        UserResponse response = UserResponse.from(user);
        /*
         * 第四步：写 Redis
         */
        userCacheService.putUser(response);
        /*
         * 返回客户端。
         */
        return response;
    }
    /*
     * ========================================
     * 修改当前用户昵称
     * ========================================
     */
    public UserResponse updateNickname(
            Long userId,
            String newNickname
    ) {
        /*
         * 这里查询的是 MySQL。
         * 为什么修改数据时不直接依赖缓存？
         * 因为： MySQL 才是 Source of Truth。
         */
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                ErrorCode.USER_NOT_FOUND
                                        )
                        );
        /*
         * 修改 Entity。
         */
        user.setNickname(newNickname);
        /*
         * 保存并立刻刷新到数据库。
         */
        User savedUser =
                userRepository
                        .saveAndFlush(user);
        /*
         * 删除 Redis Cache
         * 注意：我们不是直接相信旧缓存。
         *数据改变以后， 删除对应 Cache。
         */
        userCacheService.evict(userId);
        /*
         * 返回最新用户。
         */
        return UserResponse.from(savedUser);
    }
    /*
     * 尝试从 Cache 读取用户
     * 返回：
     * UserResponse = Cache Hit
     * null = Cache Miss
     * 如果缓存里是： __NULL__ 直接抛 USER_NOT_FOUND。
     */
    private UserResponse tryReadUserFromCache(
            Long userId
    ) {
        String cachedValue = userCacheService.getRaw(userId);
        /*
         * Redis MISS或普通 Cache Redis 故障。
         */
        if (cachedValue == null) {
            return null;
        }
        /*
         * 空值 Cache 命中。
         */
        if (
                userCacheService
                        .isNullMarker(
                                cachedValue
                        )
        ) {
            log.info("[CACHE NULL HIT] userId={}", userId);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        UserResponse cachedUser =
                userCacheService.deserialize(
                        userId,
                        cachedValue
                );
        if (cachedUser != null) {
            log.info("[CACHE HIT] userId={}", userId);
        }
        return cachedUser;
    }
    /*
     * 从 MySQL 加载，并重建 Cache
     */
    private UserResponse loadFromDatabaseAndCache(
            Long userId
    ) {
        log.info(
                "[CACHE REBUILD] Query database, userId={}",
                userId
        );
        User user =
                userRepository
                        .findById(userId)
                        .orElse(null);
        /*
         * DB 中也不存在。
         */
        if (user == null) {
            /*
             * 写短 TTL 空值 Cache。
             */
            userCacheService.putNull(userId);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        UserResponse response = UserResponse.from(user);
        /*
         * 回填 Redis。
         */
        userCacheService.putUser(response);
        return response;
    }
}