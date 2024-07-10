package com.blog.web.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author 苍晖
 * @since 2024/7/4 下午5:49
 */
@Configuration
@EnableWebSecurity
public class MySecurityConfig {

    @Autowired
    private AccessDeniedHandlerImpl accessDeniedHandler;

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置安全过滤链
     * <p>
     * 该方法配置了Spring Security的HTTP安全设置，以保护应用程序的REST API。
     * 它禁用了CSRF保护，配置了CORS处理，设定了异常处理策略，以及无状态的会话管理。
     * 对不同的API路径，它定义了不同的安全策略，如允许匿名访问，需要管理员角色等。
     * 它还添加了一个自定义的JWT认证过滤器，并配置了注销功能。
     *
     * @param http Security配置对象，用于构建安全过滤链
     * @return 构建后的SecurityFilterChain对象
     * @throws Exception 如果配置过程中出现错误
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 禁用CSRF保护
        http.csrf().disable()
                // 配置CORS跨域请求处理
                .cors().and()
                // 配置异常处理
                .exceptionHandling()
                // 指定访问被拒绝时的处理程序
                .accessDeniedHandler(accessDeniedHandler)
                .and()
                // 配置会话管理
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 配置请求授权
                .authorizeHttpRequests()
                // 指定哪些请求路径允许匿名访问
                .requestMatchers("/api/user/login", "/api/user/register").anonymous()
                // 指定哪些请求路径需要ADMIN角色
                .requestMatchers("/api/user/adduser", "/api/user/getalluser", "/api/tag/deletetag/**", "/api/media/deletemedia/**", "/api/comment/getallcomment/**", "/api/comment/updatecomment/**", "/api/category/deletecategory/**", "/api/article/deletecomment/**").hasRole("ADMIN")
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
                .and()
                // 在UsernamePasswordAuthenticationFilter之前添加自定义的JWT认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 配置注销功能
                .logout().logoutUrl("/api/user/logout")
                .permitAll();
        // 返回构建后的SecurityFilterChain对象
        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*"); // 允许所有来源
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
