package com.flashlife.service;
import com.flashlife.dto.UserCreateRequest;
import com.flashlife.entity.User;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service/*告诉 Spring UserService 是业务逻辑层组件*/
public class UserService {
    private final List<User> users = new ArrayList<>();
    private Long nextId = 1L;
    public User createUser(UserCreateRequest request){
        User user = new User(
                nextId,
                request.getUsername(),
                request.getNickname()
        );
        nextId++;
        users.add(user);
        return user;
    }
    public List<User> getAllUsers(){
        return users;
    }
    public int getUserCount() {
        return users.size();
    }
}
