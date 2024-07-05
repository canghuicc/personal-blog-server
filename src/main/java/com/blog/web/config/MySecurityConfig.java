package com.blog.web.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.web.entity.User;
import com.blog.web.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.stream.Stream;

/**
 * @author 苍晖
 * @since 2024/7/4 下午5:49
 */
@EnableWebSecurity
@Configuration
public class MySecurityConfig {

    @Autowired
    private UserMapper userMapper;

    /**
     * 配置认证管理器，用于设置用户详情服务。
     * <p>
     * 本方法的目的是将自定义的用户详情服务注册到认证管理器中。通过这种方式，认证管理器在进行用户认证时，
     * 可以使用此服务来查询用户详情，进而完成认证流程。
     *
     * @param auth AuthenticationManagerBuilder的实例，用于配置认证管理器
     * @throws Exception 如果配置过程中出现异常
     */
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 使用userDetailsService()方法返回的用户详情服务来配置认证管理器
        auth.userDetailsService(userDetailsService());
    }

    /**
     * 配置UserDetailsService Bean。
     * 此方法创建了UserDetailsService匿名子类的一个实例，该类负责根据用户名加载用户信息。
     *
     * @return UserDetailsService的实例，用于加载用户详情。
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            /**
             * 根据用户名加载用户详细信息。
             * 此方法覆盖了UserDetailsService中的loadUserByUsername方法，实现了根据用户名查询用户信息的逻辑。
             * @param username 查询的用户名
             * @return UserDetails 查询到的用户详情
             * @throws UsernameNotFoundException 如果用户未找到，则抛出此异常
             */
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                // 根据用户名在数据库中查询用户信息
                User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));

                // 如果用户不存在，抛出异常
                if (user == null) {
                    throw new UsernameNotFoundException("用户未找到");
                }

                // 将查询到的用户信息转换为Spring Security认可的格式
                org.springframework.security.core.userdetails.User springUser = new org.springframework.security.core.userdetails.User(
                        user.getUsername(), user.getPassword(),
                        Stream.of(user.getUserRole() == 1 ? "admin" : "user")
                                .map(SimpleGrantedAuthority::new)
                                .toList());

                // 返回转换后的用户详情
                return springUser;
            }
        };
    }


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
                                "api/user/adduser", "api/user/getalluser", "api/tag/deletetag/*", "api/media/deletemedia/*", "api/comment/getallcomment/*", "api/comment/updatecomment/*", "api/category/deletecategory/*", "api/article/deletecomment/*"
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
        http.cors().configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues());

        // 返回构建完成的 SecurityFilterChain 对象
        return http.build();
    }

}
