/*
 * FlashLife Day4
 * 用户表唯一性约束。
 */
/*
 * username 不能重复。
 */
ALTER TABLE users
    ADD CONSTRAINT uk_users_username
        UNIQUE (username);