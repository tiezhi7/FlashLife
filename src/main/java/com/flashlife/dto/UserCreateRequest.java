package com.flashlife.dto;

public class UserCreateRequest {
    private String username;/*用户提交的用户名。*/
    private String nickname;
    public UserCreateRequest(){
    }
    public String getUsername(){
        return username;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public String getNickname(){
        return nickname;
    }
    public void setNickname(String nickname){
        this.nickname = nickname;
    }
}
