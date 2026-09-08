package com.flashlife.config;
/*
 * PasswordEncoder： Spring Security 提供的密码编码接口。
 */
import org.springframework.security.crypto.password.PasswordEncoder;
/*
 * BCryptPasswordEncoder：PasswordEncoder 的 BCrypt 实现。
 */
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/*
 * @Configuration
 *告诉 Spring：这是一个配置类。
 */
@Configuration
public class PasswordConfig {
    /*
     * @Bean
     * 告诉 Spring：创建一个 PasswordEncoder 对象，并交给 Spring 容器管理。
     * 以后其他 Service 需要 PasswordEncoder，Spring 就可以自动注入。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        /*
         * 使用 BCrypt 实现。
         */
        return new BCryptPasswordEncoder();
    }
}