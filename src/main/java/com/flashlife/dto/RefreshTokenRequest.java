package com.flashlife.dto;

import jakarta.validation.constraints.NotBlank;

/*
 * 刷新 Access Token 请求。
 */
public class RefreshTokenRequest {
    @NotBlank(
            message = "Refresh Token不能为空"
    )
    private String refreshToken;
    public RefreshTokenRequest() {
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
