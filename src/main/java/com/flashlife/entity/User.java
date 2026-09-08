package com.flashlife.entity;
// @Column
// 用于描述数据库字段的一些规则。
import jakarta.persistence.Column;
// @Entity
// 表示当前 Java 类是 JPA Entity。
import jakarta.persistence.Entity;
// @GeneratedValue
// 用于配置主键如何生成。
import jakarta.persistence.GeneratedValue;
// GenerationType
// 包含不同的主键生成策略。
import jakarta.persistence.GenerationType;
// @Id
// 表示当前字段是主键。
import jakarta.persistence.Id;
// @Table
// 用于指定当前 Entity 对应哪张数据库表。
import jakarta.persistence.Table;
/*
 * User Entity
 * 它表示 FlashLife 中的用户。
 * 从 Day3 开始：
 * User Java 类会和MySQL users 表建立映射关系。
 */
@Entity
/*
 * 当前 User 对应数据库：
 * users 表
 */
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            nullable = false,
            length = 50,
            unique = true
    )
    private String username;

    /*
     * 注意字段名：passwordHash而不是 password。
     * Java 对象中同样明确表达：这里绝不保存明文密码。
     */
    @Column(
            name = "password_hash",
            nullable = false,
            length = 100
    )
    private String passwordHash;

    @Column(
            nullable = false,
            length = 50
    )
    private String nickname;
    /*
     * 无参构造方法。
     * JPA 创建 Entity 时通常需要它。
     */
    public User() {
    }
    /*
     * 我们自己使用的有参构造方法。
     * 注意：不再传 id。
     * 因为：MySQL AUTO_INCREMENT会生成 id。
     */
    public User(
            String username,
            String passwordHash,
            String nickname
    ) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(
            String passwordHash
    ) {
        this.passwordHash = passwordHash;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
