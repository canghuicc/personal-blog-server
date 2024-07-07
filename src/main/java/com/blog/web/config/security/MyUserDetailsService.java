package com.blog.web.config.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.web.entity.User;
import com.blog.web.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author 苍晖
 * @since 2024/7/7 下午4:06
 */
@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据用户名加载用户详细信息。
     * 此方法是Spring Security框架中UserDetailsService接口的实现方法之一，用于根据提供的用户名查找用户信息。
     * 如果找不到匹配的用户，将抛出UsernameNotFoundException异常。
     *
     * @param username 用户名，用于查找用户信息。
     * @return UserDetails对象，包含查找到的用户详细信息。
     * @throws UsernameNotFoundException 如果找不到匹配的用户，则抛出此异常。
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询用户信息
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        // 如果用户不存在，则抛出异常
        if (user == null){
            throw new UsernameNotFoundException("用户不存在");
        }

        // 根据用户角色设置权限列表
        List<GrantedAuthority> authorities;
        if (user.getUserRole() == 1){
            authorities=Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }else {
            authorities=Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        // 创建并返回UserDetails对象，包含用户名、密码和权限信息
        return new org.springframework.security.core.userdetails.User(username, user.getPassword(), authorities);
    }

}
