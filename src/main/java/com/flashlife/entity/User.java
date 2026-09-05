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
    /*
     * @Id
     * 告诉 JPA：
     * id 是主键。
     * 对应 MySQL：
     * PRIMARY KEY
     */
    @Id
    /*
     * IDENTITY：
     * 表示 ID 由数据库生成。
     * 对应我们 MySQL 的：
     * AUTO_INCREMENT
     */
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;
    /*
     * unique = true
     * 表达：username 在数据库设计中应当唯一。
     *注意：真正可靠的唯一性仍然由数据库 UNIQUE 约束保证。
     */
    @Column(
            nullable = false,
            length = 50,
            unique = true
    )
    private String username;
    /*
     * nickname
     * 同样对应数据库中的： nickname VARCHAR(50) NOT NULL
     */
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
            String nickname
    ) {
        this.username = username;
        this.nickname = nickname;
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
