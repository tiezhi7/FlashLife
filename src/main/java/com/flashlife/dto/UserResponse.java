package com.flashlife.dto;
import com.flashlife.entity.User;
/*
 * UserResponse专门表示：“哪些 User 信息允许返回给客户端。”
 * 注意：这里故意没有 passwordHash。
 */
public class UserResponse {
    private Long id;
    private String username;
    private String nickname;
    public UserResponse() {
    }
    public UserResponse(
            Long id,
            String username,
            String nickname
    ) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
    }


    /*
     * 把 User Entity
     * 转换为安全的 UserResponse。
     */
    public static UserResponse from(
            User user
    ) {

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname()
        );
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