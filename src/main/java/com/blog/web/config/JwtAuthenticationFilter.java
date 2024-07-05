package com.blog.web.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author 苍晖
 * @since 2024/7/4 下午8:39
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractTokenFromRequest(request);
            if (token != null && validateToken(token)) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(token, null);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
        } catch (Exception e) {
            logger.error("令牌无效或过期");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean validateToken(String token) {
        String tokenKey;
        if (token.startsWith("user")) {
            tokenKey = "user_token";
        } else if (token.startsWith("AdminUser")) {
            tokenKey = "AdminUser_token";
        } else {
            return false;
        }
        String storedToken = (String) redisTemplate.opsForValue().get(tokenKey);
        return token.equals(storedToken);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader("X-Token");
        if (token != null) {
            return token.substring(7);
        }
        return null;
    }
}
