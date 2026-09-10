package com.flashlife.dto;
/*
 * LoginResponse登录成功后返回的数据。
 */
public class LoginResponse {
    /*
     * JWT Access Token
     * 客户端后续请求需要携带它。
     */
    private String accessToken;
    /*
     * Token 类型。
     * 我们使用：
     * Bearer
     */
    private String tokenType;
    /*
     * Token 还有多少秒过期。
     * 例如：
     * 1800 秒=30 分钟
     */
    private long expiresInSeconds;
    /*
     * 当前登录用户的安全公开信息。
     * 不包含 passwordHash。
     */
    private UserResponse user;
    public LoginResponse() {
    }
    public LoginResponse(
            String accessToken,
            String tokenType,
            long expiresInSeconds,
            UserResponse user
    ) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresInSeconds = expiresInSeconds;
        this.user = user;
    }
    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(
            String accessToken
    ) {
        this.accessToken = accessToken;
    }
    public String getTokenType() {
        return tokenType;
    }
    public void setTokenType(
            String tokenType
    ) {
        this.tokenType = tokenType;
    }
    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }
    public void setExpiresInSeconds(
            long expiresInSeconds
    ) {
        this.expiresInSeconds = expiresInSeconds;
    }
    public UserResponse getUser() {
        return user;
    }
    public void setUser(
            UserResponse user
    ) {
        this.user = user;
    }
}
