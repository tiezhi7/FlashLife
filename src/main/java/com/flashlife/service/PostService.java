package com.flashlife.service;

import com.flashlife.dto.PostResponse;

import com.flashlife.entity.Post;

import com.flashlife.exception.BusinessException;
import com.flashlife.exception.ErrorCode;

import com.flashlife.repository.PostRepository;

import org.springframework.stereotype.Service;
/*
 * 帖子业务层。
 */
@Service
public class PostService {
    private final PostRepository postRepository;
    public PostService(
            PostRepository postRepository
    ) {

        this.postRepository = postRepository;
    }
    /*
     * 根据 ID 查询帖子。
     */
    public PostResponse getPostById(
            Long postId
    ) {
        Post post =
                postRepository
                        .findById(postId)
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                ErrorCode.POST_NOT_FOUND
                                        )
                        );
        return PostResponse.from(post);
    }
    /*
     * 给其他 Service 使用。
     * 用于确认帖子确实存在。
     */
    public void ensurePostExists(
            Long postId
    ) {
        if (
                !postRepository.existsById(postId)
        ) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
    }
}
