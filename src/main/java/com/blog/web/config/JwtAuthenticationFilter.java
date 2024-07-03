//package com.blog.web.config;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
///**
// * @author 苍晖
// * @since 2024/7/4 下午8:39
// */
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//    /**
//     * 处理过滤逻辑，核心是验证请求中的令牌(token)。
//     * 如果令牌有效，设置认证信息；否则，返回未授权状态。
//     *
//     * @param request     HTTP请求，从中提取令牌。
//     * @param response    HTTP响应，用于返回状态码。
//     * @param filterChain 过滤器链，继续请求处理。
//     * @throws ServletException 如果发生Servlet相关异常。
//     * @throws IOException      如果发生IO相关异常。
//     */
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        try {
//            // 从请求中提取令牌。
//            String token = extracTokenFromRequest(request);
//            // 验证令牌是否有效。
//            if (token != null && validateToken(token)) {
//                // 创建认证令牌，并设置到安全上下文中。
//                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(token, null);
//                SecurityContextHolder.getContext().setAuthentication(auth);
//            } else {
//                // 令牌无效，返回未授权状态。
//                response.setStatus(HttpStatus.UNAUTHORIZED.value());
//                return;
//            }
//        } catch (Exception e) {
//            // 验证过程中发生异常，记录日志并返回未授权状态。
//            logger.error("token验证失败");
//            response.setStatus(HttpStatus.UNAUTHORIZED.value());
//            return;
//        }
//
//        // 继续请求链的处理。
//        filterChain.doFilter(request, response);
//    }
//
//
//    /**
//     * 验证令牌的有效性。
//     * 通过比较存储在Redis中的令牌和传入的令牌是否一致来验证令牌的有效性。
//     * 这个方法主要用于确保当前操作的用户身份是经过验证的，防止未授权的访问。
//     *
//     * @param token 待验证的令牌字符串。
//     * @return 如果传入的令牌与存储的令牌相等，则返回true；否则返回false。
//     */
//    private boolean validateToken(String token) {
//        // 定义token前缀对应的Redis键
//        String tokenKey;
//
//        // 判断令牌前缀并分配对应的Redis键
//        if (token.startsWith("user")) {
//            tokenKey = "user_token";
//        } else if (token.startsWith("AdminUser")) {
//            tokenKey = "AdminUser_token";
//        } else {
//            // 如果令牌前缀不符合预期，可选择抛出异常或直接返回false
//            // 这里选择直接返回false表示验证不通过
//            return false;
//        }
//
//        // 从Redis中获取存储的用户令牌
//        String storedToken = (String) redisTemplate.opsForValue().get(tokenKey);
//
//        // 比较传入的令牌和存储的令牌是否相等
//        return token.equals(storedToken);
//    }
//
//
//    /**
//     * 从HTTP请求中提取Bearer令牌。
//     *
//     * @param request HTTP请求对象，用于获取请求头信息。
//     * @return 提取的Bearer令牌字符串，如果请求中不存在或格式不正确，则返回null。
//     */
//    private String extracTokenFromRequest(HttpServletRequest request) {
//        // 尝试从请求头中获取Authorization信息
//        String bearerToken = request.getHeader("Authorization");
//
//        // 检查获取的Authorization信息是否以"Bearer "开头，是则进一步处理
//        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//            // 从Authorization信息中提取出Bearer令牌部分
//            return bearerToken.substring(7);
//        }
//
//        // 如果Authorization信息不存在或不以"Bearer "开头，则返回null
//        return null;
//    }
//
//}
