package com.blog.web.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author 苍晖
 * @since 2024/7/4 下午5:49
 */
@Configuration
@EnableWebSecurity
public class MySecurityConfig {

    @Autowired
    private AccessDeniedHandlerImpl unauthorizedHandler;

    @Autowired
    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .exceptionHandling()
                .accessDeniedHandler(unauthorizedHandler)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests()
                .requestMatchers("/api/user/login", "/api/user/admin/login", "/api/user/register").anonymous()
                .requestMatchers("/api/user/adduser", "/api/user/getalluser", "/api/tag/deletetag/**", "/api/media/deletemedia/**", "/api/comment/getallcomment/**", "/api/comment/updatecomment/**", "/api/category/deletecategory/**", "/api/article/deletecomment/**").hasRole("ROLE_ADMIN")
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout().logoutUrl("/api/user/logout")
                .logoutSuccessHandler(logoutSuccessHandler)
                .permitAll()
                .and()
                .cors();
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
