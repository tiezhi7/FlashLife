CREATE TABLE IF NOT EXISTS refresh_tokens (

                                              id BIGINT PRIMARY KEY AUTO_INCREMENT,

                                              user_id BIGINT NOT NULL,

                                              token_hash CHAR(64) NOT NULL,

    expires_at DATETIME(6) NOT NULL,

    revoked BOOLEAN NOT NULL DEFAULT FALSE,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT uk_refresh_tokens_token_hash
    UNIQUE (token_hash),

    CONSTRAINT fk_refresh_tokens_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE,

    INDEX idx_refresh_tokens_user_id (user_id),

    INDEX idx_refresh_tokens_expires_at (expires_at)
    );