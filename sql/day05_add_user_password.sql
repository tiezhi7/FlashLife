/*
 * FlashLife Day5 为用户表增加密码哈希字段。
 * 注意：正式生产环境的数据迁移不能直接删除用户数据。当前 DELETE 仅用于本地学习测试数据。
 */
/*
 * 本地学习环境清理旧测试用户。
 */
DELETE FROM users;
/*
 * 重置本地学习环境自增 ID。
 */
ALTER TABLE users AUTO_INCREMENT = 1;
/*
 * 增加 BCrypt 密码哈希字段。
 */
ALTER TABLE users
    ADD COLUMN password_hash VARCHAR(100) NOT NULL
    AFTER username;