package com.flashlife.config;

import com.flashlife.security.JwtAuthenticationEntryPoint;
import com.flashlife.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
/*
 * SecurityConfig
 * FlashLife 安全配置中心。
 */
@Configuration
public class SecurityConfig {
    private final JwtAuthenticationFilter
            jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint
            jwtAuthenticationEntryPoint;
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
        http
                /*
                 * ==================================================
                 * CSRF
                 * ==================================================
                 * 当前 FlashLife 是：
                 * REST API
                 * +
                 * Authorization Bearer Token
                 * +
                 * Stateless
                 * 今天暂时关闭 CSRF。
                 * 注意：
                 * “关闭 CSRF”
                 * 不是所有项目都应该这么做。
                 * Cookie / Session 认证体系
                 * 需要重新认真考虑 CSRF。
                 */
                .csrf(
                        AbstractHttpConfigurer::disable
                )
                /*
                 * 不使用 Spring 默认表单登录。
                 */
                .formLogin(
                        AbstractHttpConfigurer::disable
                )
                /*
                 * 不使用 HTTP Basic。
                 */
                .httpBasic(
                        AbstractHttpConfigurer::disable
                )
                /*
                 * ==================================================
                 * Session
                 * ==================================================
                 * JWT API：
                 * 每次请求自己携带 Token。
                 * 所以不依赖服务器 Session保存登录状态。
                 */
                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )
                /*
                 * 未登录访问受保护接口时：使用我们自己的
                 * JwtAuthenticationEntryPoint返回统一 JSON。
                 */
                .exceptionHandling(
                        exception ->
                                exception.authenticationEntryPoint(
                                        jwtAuthenticationEntryPoint
                                )
                )
                /*
                 * ==================================================
                 * URL 权限规则
                 * ==================================================
                 */
                .authorizeHttpRequests(
                        auth ->
                                auth
                                        /*
                                         * 注册和登录必须允许匿名访问。
                                         * 否则：
                                         * “必须登录才能登录”
                                         * 就会形成死循环。
                                         */
                                        .requestMatchers(
                                                "/api/auth/**"
                                        )
                                        .permitAll()
                                        /*
                                         * Day1 的一些健康检查接口
                                         * 可以继续公开。
                                         */
                                        .requestMatchers(
                                                "/api/hello",
                                                "/api/health",
                                                "/api/info",
                                                "/error"
                                        )
                                        .permitAll()
                                        /*
                                         * 其他接口：
                                         * 必须已经认证。
                                         */
                                        .anyRequest()
                                        .authenticated()
                )
                /*
                 * JwtAuthenticationFilter
                 * 放在 Spring Security
                 * UsernamePasswordAuthenticationFilter
                 * 前面执行。
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }
}
