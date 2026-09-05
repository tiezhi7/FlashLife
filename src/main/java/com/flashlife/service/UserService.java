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

import com.flashlife.exception.BusinessException;
import com.flashlife.exception.ErrorCode;

import org.springframework.dao.DataIntegrityViolationException;
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
     * 创建用户。
     */
    public User createUser(
            UserCreateRequest request
    ) {
        /*
         * 第一道防线： 在 Service 先查询 username是否已经存在。
         * 这样可以给用户非常清晰的错误提示。
         */
        boolean exists = userRepository.existsByUsername(
                request.getUsername()
        );
        /*
         * 如果已经存在：不再创建 User。直接抛业务异常。
         */
        if (exists) {
            throw new BusinessException(
                    ErrorCode.USERNAME_ALREADY_EXISTS
            );
        }
        User user = new User(
                request.getUsername(),
                request.getNickname()
        );
        /*
         * 第二道防线： 即使在极端并发情况下：两个请求同时通过 existsByUsername()
         * 数据库 UNIQUE仍然会阻止重复插入。
         */
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            /*
             * 数据库发现 UNIQUE 冲突后，
             * 我们把底层数据库异常转换成用户能理解的业务异常。
             */
            throw new BusinessException(
                    ErrorCode.USERNAME_ALREADY_EXISTS
            );
        }
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
        return userRepository
                .findById(id)
                .orElseThrow(
                        () -> new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }
}
