package com.flashlife.service;
// 创建用户请求 DTO。
import com.flashlife.dto.UserCreateRequest;
// User Entity。
import com.flashlife.entity.User;
// User 数据访问层。
import com.flashlife.repository.UserRepository;
// @Service：
// 告诉 Spring 这是业务逻辑层组件。
import org.springframework.stereotype.Service;
// Java List。
import java.util.List;
/*
 * UserService
 * 用户业务逻辑层。
 * Controller 不直接操作数据库。
 * Controller
 *     ↓
 * Service
 *     ↓
 * Repository
 *     ↓
 * MySQL
 */
@Service
public class UserService {
    /*
     * userRepository
     * 专门负责用户数据库操作。
     */
    private final UserRepository userRepository;
    /*
     * 构造器注入。
     * Spring 会自动创建并提供：
     * UserRepository
     */
    public UserService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }
    /*
     * ================================
     * 创建用户
     * ================================
     */
    public User createUser(
            UserCreateRequest request
    ) {
        /*
         * 根据 DTO 创建 User Entity。
         * 注意：这里不再生成 id。
         */
        User user = new User(
                request.getUsername(),
                request.getNickname()
        );
        /*
         * 保存到 MySQL。
         * save()
         * 最终会让 Hibernate执行类似：INSERT INTO users ...
         */
        return userRepository.save(user);
    }
    /*
     * ================================
     * 查询全部用户
     * ================================
     */
    public List<User> getAllUsers() {
        /*
         * findAll()
         * 最终会查询： users 表
         */
        return userRepository.findAll();
    }
    /*
     * ================================
     * 查询用户数量
     * ================================
     */
    public long getUserCount() {
        /*
         * count()
         * 统计 MySQL users 表一共有多少条记录。
         */
        return userRepository.count();
    }
    /*
     * ================================
     * 根据 ID 查询用户
     * ================================
     */
    public User getUserById(Long id) {
        /*
         * findById(id)
         * 会根据数据库主键查询。
         * 如果存在：返回 User
         * 如果不存在：暂时返回 null
         * Day4 会用正规的业务异常替代 null。
         */
        return userRepository
                .findById(id)
                .orElse(null);
    }
}
