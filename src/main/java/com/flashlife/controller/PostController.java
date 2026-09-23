package com.flashlife.controller;


import com.flashlife.common.Result;

import com.flashlife.dto.PostResponse;

import com.flashlife.service.PostService;

import com.flashlife.dto.LikeResponse;

import com.flashlife.service.PostLikeService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/*
 * Post API。
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;
    private final PostLikeService postLikeService;
    public PostController(
            PostService postService,
            PostLikeService postLikeService
    ) {
        this.postService = postService;
        this.postLikeService = postLikeService;
    }
    /*
     * GET /api/posts/1
     */
    @GetMapping("/{postId}")
    public Result<PostResponse> getPost(
            @PathVariable
            Long postId
    ) {
        return Result.success(postService.getPostById(postId));
    }
    /*
     * 点赞帖子
     * POST /api/posts/1/like
     */
    @PostMapping("/{postId}/like")
    public Result<LikeResponse> like(
            @PathVariable
            Long postId,
            /*
             * 当前登录用户 ID。
             * 来自：
             * JWT
             * ↓
             * JwtAuthenticationFilter
             * ↓
             * SecurityContext
             */
            @AuthenticationPrincipal
            Long userId
    ) {
        return Result.success(
                postLikeService.like(postId, userId)
        );
    }
    /*
     * 取消点赞
     */
    @DeleteMapping("/{postId}/like")
    public Result<LikeResponse> unlike(
            @PathVariable
            Long postId,
            @AuthenticationPrincipal
            Long userId
    ) {
        return Result.success(
                postLikeService.unlike(postId, userId)
        );
    }
    /*
     * 查询当前用户是否点赞
     */
    @GetMapping("/{postId}/like/me")
    public Result<LikeResponse> getMyLikeStatus(
            @PathVariable
            Long postId,
            @AuthenticationPrincipal
            Long userId
    ) {
        return Result.success(
                postLikeService
                        .getMyLikeStatus(postId, userId)
        );
    }
    /*
     * 查询点赞数
     */
    @GetMapping("/{postId}/likes/count")
    public Result<Long> getLikeCount(
            @PathVariable
            Long postId
    ) {
        return Result.success(
                postLikeService
                        .getLikeCount(postId)
        );
    }
}
