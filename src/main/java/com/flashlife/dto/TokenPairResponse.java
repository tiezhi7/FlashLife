package com.flashlife.dto;
/*
 * TokenPairResponse
 * Token Pair：Access Token+Refresh Token
 */
public class TokenPairResponse {
    /*
     * 用于访问业务接口。
     */
    private String accessToken;
    /*
     * 用于刷新登录状态。
     */
    private String refreshToken;
    /*
     * Bearer
     */
    private String tokenType;
    /*
     * Access Token 剩余有效秒数。
     */
    private long accessExpiresInSeconds;
    /*
     * Refresh Token 剩余有效秒数。
     */
    private long refreshExpiresInSeconds;
    /*
     * 当前用户公开信息。
     */
    private UserResponse user;
    public TokenPairResponse() {
    }
    public TokenPairResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long accessExpiresInSeconds,
            long refreshExpiresInSeconds,
            UserResponse user
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.accessExpiresInSeconds = accessExpiresInSeconds;
        this.refreshExpiresInSeconds = refreshExpiresInSeconds;
        this.user = user;
    }
    public String getAccessToken() {
        return accessToken;
    }
    public String getRefreshToken() {
        return refreshToken;
    }
    public String getTokenType() {
        return tokenType;
    }
    public long getAccessExpiresInSeconds() {
        return accessExpiresInSeconds;
    }
    public long getRefreshExpiresInSeconds() {
        return refreshExpiresInSeconds;
    }
    public UserResponse getUser() {
        return user;
    }
}