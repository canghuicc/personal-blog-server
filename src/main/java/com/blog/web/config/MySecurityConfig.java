package com.blog.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author 苍晖
 * @since 2024/7/4 下午5:49
 */
public class MySecurityConfig {

    /**
     * 配置安全过滤链，定义应用程序的访问控制策略。
     *
     * @param http 安全配置对象，用于构建安全过滤链。
     * @return SecurityFilterChain，定义了应用程序的所有安全过滤规则。
     * @throws Exception 如果配置过程中出现错误。
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 配置请求授权规则
        http
                .authorizeHttpRequests((requests) -> requests
                        // 允许所有用户访问 /login, /admin/login 和 /register 端点
                        .requestMatchers(
                                "api/user/login", "api/user/admin/login", "api/user/register"
                        ).permitAll()
                        // 仅允许具有 "admin" 角色的用户访问 /adduser 和 /getalluser 端点
                        .requestMatchers(
                                "api/user/adduser", "api/user/getalluser","api/tag/deletetag/*","api/media/deletemedia/*","api/comment/getallcomment/*","api/comment/updatecomment/*","api/category/deletecategory/*","api/article/deletecomment/*"
                        ).hasRole("admin")
                        // 其他端点需要认证访问
                        .anyRequest().authenticated()
                )
                // 配置基于 Token 的认证
                .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .oauth2ResourceServer()
                .jwt();
        // 禁用 CSRF 保护，适用于 API
        http.csrf().disable();

        // 返回构建完成的 SecurityFilterChain 对象
        return http.build();
    }

}
