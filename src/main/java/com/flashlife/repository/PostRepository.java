package com.flashlife.repository;

import com.flashlife.entity.Post;

import org.springframework.data.jpa.repository.JpaRepository;
/*
 * Post 数据访问层。
 */
public interface PostRepository
        extends JpaRepository<Post, Long> {
}
