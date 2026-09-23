USE flashlife;
-- posts
CREATE TABLE IF NOT EXISTS posts (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     user_id BIGINT NOT NULL,
                                     title VARCHAR(100) NOT NULL,

    content VARCHAR(1000) NOT NULL,
    created_at DATETIME(6)
    NOT NULL
    DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_posts_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE,
    INDEX idx_posts_user_id(user_id),
    INDEX idx_posts_created_at(created_at)
    );
-- post_likes
CREATE TABLE IF NOT EXISTS post_likes (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          post_id BIGINT NOT NULL,
                                          user_id BIGINT NOT NULL,
                                          created_at DATETIME(6)
    NOT NULL
    DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_post_likes_post
    FOREIGN KEY (post_id)
    REFERENCES posts(id)
    ON DELETE CASCADE,
    CONSTRAINT fk_post_likes_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE,
    CONSTRAINT uk_post_likes_post_user
    UNIQUE (post_id, user_id),
    INDEX idx_post_likes_post_id(post_id),
    INDEX idx_post_likes_user_id(user_id)
    );