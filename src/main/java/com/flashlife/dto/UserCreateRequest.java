package com.flashlife.dto;
/*
 * @NotBlank用于验证字符串：
 *不能是 null
 * 不能是 ""
 * 不能只有空格
 */
import jakarta.validation.constraints.NotBlank;
/*
 * @Pattern使用“正则表达式”
 * 限制字符串的字符组成。
 */
import jakarta.validation.constraints.Pattern;
/*
 * @Size用于限制字符串长度。
 */
import jakarta.validation.constraints.Size;
/*
 * 创建用户请求 DTO。
 * 前端例如发送：
 * {
 *     "username": "tom",
 *     "nickname": "Tom"
 * }
 */
public class UserCreateRequest {
    /*
     * 用户名不能为空。
     * 下面这些都会失败：
     * null
     * ""
     * "   "
     */
    @NotBlank(
            message = "用户名不能为空"
    )
    /*
     * 用户名长度：最少 3 个字符，最多 20 个字符
     */
    @Size(
            min = 3,
            max = 20,
            message = "用户名长度必须在3到20个字符之间"
    )
    /*
     * 用户名字符限制。
     * ^                从字符串开头开始
     * [a-zA-Z0-9_]     只允许：
     *                  小写字母
     *                  大写字母
     *                  数字
     *                  下划线
     *
     * +                至少出现一次
     *
     * $                一直到字符串结尾
     */
    @Pattern(
            regexp = "^[a-zA-Z0-9_]+$",
            message = "用户名只能包含字母、数字和下划线"
    )
    private String username;
    /*
     * 昵称不能为空。
     */
    @NotBlank(
            message = "昵称不能为空"
    )
    /*
     * 昵称最多 30 个字符。
     */
    @Size(
            max = 30,
            message = "昵称长度不能超过30个字符"
    )
    private String nickname;
    /*
     * 无参构造方法。
     * JSON → Java DTO
     * 时会使用。
     */
    public UserCreateRequest() {
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
