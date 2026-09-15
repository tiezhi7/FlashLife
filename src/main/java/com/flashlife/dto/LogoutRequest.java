package com.flashlife.dto;

import jakarta.validation.constraints.NotBlank;
/*
 * 用户退出登录请求。
 */
public class LogoutRequest {
    @NotBlank(
            message = "Refresh Token不能为空"
    )
    private String refreshToken;
    public LogoutRequest() {
    }
    public String getRefreshToken() {
        return refreshToken;
    }
    public void setRefreshToken(
            String refreshToken
    ) {
        this.refreshToken = refreshToken;
    }
}
