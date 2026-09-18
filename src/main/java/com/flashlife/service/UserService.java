package com.flashlife.service;

import com.flashlife.dto.UserResponse;

import com.flashlife.entity.User;

import com.flashlife.exception.BusinessException;
import com.flashlife.exception.ErrorCode;

import com.flashlife.repository.UserRepository;

import org.springframework.stereotype.Service;

import com.flashlife.cache.UserCacheService;

import java.util.List;
/*
 * 用户业务逻辑。
 * Day5：注册/登录已经移动到 AuthService。
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserCacheService userCacheService;
    public UserService(
            UserRepository userRepository,
            UserCacheService userCacheService
    ) {
        this.userRepository = userRepository;
        this.userCacheService = userCacheService;
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
}