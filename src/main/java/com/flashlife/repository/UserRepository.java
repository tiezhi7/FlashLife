package com.flashlife.repository;
// 导入 User Entity。
import com.flashlife.entity.User;
// JpaRepository
// Spring Data JPA 提供的核心 Repository 接口。
import org.springframework.data.jpa.repository.JpaRepository;
/*
 * UserRepository
 * 专门负责：User与MySQL users 表之间的数据访问。
 */
public interface UserRepository
        extends JpaRepository<User, Long> {
    /*
     * 判断 username 是否已经存在。
     * 例如：existsByUsername("tom")
     *如果数据库已经有 tom： true
     * 如果没有 alice：false
     */
    boolean existsByUsername(String username);
}