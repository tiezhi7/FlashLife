package com.flashlife.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
/*
 * 用户注册请求。
 */
public class RegisterRequest {
    /*
     * username： 3～20字符 字母 / 数字 / 下划线
     */
    @NotBlank(
            message = "用户名不能为空"
    )
    @Size(
            min = 3,
            max = 20,
            message = "用户名长度必须在3到20个字符之间"
    )
    @Pattern(
            regexp = "^[a-zA-Z0-9_]+$",
            message = "用户名只能包含字母、数字和下划线"
    )
    private String username;
    /*
     * 密码： 不允许为空。
     * 当前学习阶段：长度要求 8～64。
     * 今天暂时不强制： 大小写 + 特殊字符。后面可以升级密码策略。
     */
    @NotBlank(
            message = "密码不能为空"
    )
    @Size(
            min = 8,
            max = 64,
            message = "密码长度必须在8到64个字符之间"
    )
    private String password;
    @NotBlank(
            message = "昵称不能为空"
    )
    @Size(
            max = 30,
            message = "昵称长度不能超过30个字符"
    )
    private String nickname;
    public RegisterRequest() {
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}