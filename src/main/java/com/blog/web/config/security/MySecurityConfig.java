package com.blog.web.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * @author 苍晖
 * @since 2024/7/4 下午5:49
 */
@Configuration
@EnableWebSecurity
public class MySecurityConfig {
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.csrf().disable()
//                .exceptionHandling(configurer -> configurer.authenticationEntryPoint(jwtAuthenticationEntryPoint))
//                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers("/api/user/login", "/api/user/admin/login", "/api/user/register").permitAll()
//                        .requestMatchers( "/api/user/adduser","/api/user/getalluser", "/api/tag/deletetag/**",
//                                "/api/media/deletemedia/**", "/api/comment/getallcomment/**",
//                                "/api/comment/updatecomment/**", "/api/category/deletecategory/**",
//                                "/api/article/deletecomment/**").hasRole("admin")
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
//        return http.build();
}
