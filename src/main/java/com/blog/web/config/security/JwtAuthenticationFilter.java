package com.blog.web.config.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author 苍晖
 * @since 2024/7/7 下午6:13
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private  static final long JWT_REFRESH_THRESHOLD = 5 * 60 * 1000L; // 到期前 5 分钟刷新

    @Autowired
    private JwtUtilService jwtUtilService;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String token = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // The part after "Bearer "
            username = jwtUtilService.extractUsername(token);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtUtilService.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        if (username != null && token != null && jwtUtilService.isTokenRefreshNeeded(token, JWT_REFRESH_THRESHOLD)) {
            String refreshedToken = jwtUtilService.refreshToken(token);
            response.setHeader("Authorization", "Bearer " + refreshedToken);
            redisTemplate.opsForValue().set("token:" + username, refreshedToken);
        }

        filterChain.doFilter(request, response);
    }
}