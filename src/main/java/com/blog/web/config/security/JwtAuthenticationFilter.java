package com.blog.web.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author 苍晖
 * @since 2024/7/7 下午6:13
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtilService jwtUtilService;

    @Autowired
    private MyUserDetailsService userDetailsService;

    /**
     * 处理过滤请求以进行JWT认证。
     * 从请求头中提取令牌，刷新令牌，并在认证成功后设置安全上下文。
     * 如果用户已认证，则直接通过过滤器链。
     *
     * @param request     HTTP请求，用于获取请求头中的令牌。
     * @param response    HTTP响应，当前方法中不直接使用，但必须作为参数传递给过滤器链。
     * @param filterChain 过滤器链，用于继续处理请求。
     * @throws ServletException 如果过滤器处理过程中出现Servlet相关异常。
     * @throws IOException      如果过滤器处理过程中出现IO相关异常。
     */
    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 从请求头中获取令牌
        final String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // 如果Authorization头存在且以"Bearer "开始，则进行处理
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // 去掉"Bearer "前缀
            // 从令牌中提取用户名
            username = jwtUtilService.extractUsernameFromToken(token);
            // 刷新令牌
            token = jwtUtilService.refreshToken(token, 5);
        }

        // 如果用户名不为空且安全上下文中没有认证信息，则进行认证
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            // 验证令牌的有效性
            if (jwtUtilService.validateToken(token, userDetails)) {
                // 创建新的认证令牌
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                // 设置认证细节
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // 设置安全上下文的认证信息
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

}