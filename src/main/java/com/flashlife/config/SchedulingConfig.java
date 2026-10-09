package com.flashlife.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
/*
 * 开启 Spring 定时任务功能。
 * 之后：@Scheduled才会生效。
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
