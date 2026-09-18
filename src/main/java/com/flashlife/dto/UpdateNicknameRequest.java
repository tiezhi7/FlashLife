package com.flashlife.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
/*
 * 修改用户昵称请求。
 */
public class UpdateNicknameRequest {
    /*
     * 昵称不能： null "" "   "
     */
    @NotBlank(message = "昵称不能为空")
    /*
     * 最长 30 字符。
     */
    @Size(
            max = 30,
            message = "昵称长度不能超过30个字符"
    )
    private String nickname;
    public UpdateNicknameRequest() {}
    public String getNickname() {return nickname;}
    public void setNickname(
            String nickname
    ) {
        this.nickname = nickname;}
}
