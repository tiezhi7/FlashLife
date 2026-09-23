package com.flashlife.service;


import com.flashlife.dto.LikeResponse;


import org.springframework.stereotype.Service;
/*
 * PostLikeService点赞业务流程。
 */
@Service
public class PostLikeService {
    private final PostService postService;
    private final PostLikeRedisService postLikeRedisService;
    public PostLikeService(
            PostService postService,
            PostLikeRedisService postLikeRedisService
    ) {
        this.postService = postService;
        this.postLikeRedisService = postLikeRedisService;
    }
    /*
     * 点赞
     */
    public LikeResponse like(
            Long postId,
            Long userId
    ) {
        /*
         * 先确认帖子存在。
         * 防止： 给不存在的 postId创建 Redis 数据。
         */
        postService.ensurePostExists(postId);
        /*
         * Redis Lua：SADD+INCR
         * 原子完成。
         */
        long count = postLikeRedisService.like(postId, userId);
        return new LikeResponse(true, count);
    }
    /*
     * 取消点赞
     */
    public LikeResponse unlike(
            Long postId,
            Long userId
    ) {
        postService.ensurePostExists(postId);
        long count = postLikeRedisService.unlike(postId, userId);
        return new LikeResponse(false, count);
    }
    /*
     * 查询自己是否点赞
     */
    public LikeResponse getMyLikeStatus(
            Long postId,
            Long userId
    ) {
        postService.ensurePostExists(postId);
        boolean liked = postLikeRedisService.hasLiked(postId, userId);
        long count = postLikeRedisService.getLikeCount(postId);
        return new LikeResponse(liked, count);
    }
    /*
     * 点赞数量
     */
    public long getLikeCount(
            Long postId
    ) {
        postService.ensurePostExists(postId);
        return postLikeRedisService
                .getLikeCount(postId);
    }
}