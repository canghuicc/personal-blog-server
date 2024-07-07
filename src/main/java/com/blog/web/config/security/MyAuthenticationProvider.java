package com.blog.web.config.security;

import com.blog.web.config.MyPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * @author 苍晖
 * @since 2024/7/7 下午4:30
 */
@Component
public class MyAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    MyUserDetailsService myUserDetailsService;

    @Autowired
    MyPasswordEncoder myPasswordEncoder;

    /**
     * 尝试验证给定的认证信息是否有效。
     *
     * 此方法在收到包含用户名和密码的认证请求后，会尝试验证这些信息是否匹配数据库中的用户记录。
     * 如果认证失败，即用户名或密码不正确，将抛出AuthenticationException异常。
     * 如果认证成功，将返回一个新的UsernamePasswordAuthenticationToken对象，包含验证成功的用户详细信息。
     *
     * @param authentication 提供的认证信息，包含用户名和密码。
     * @return 经过验证的UsernamePasswordAuthenticationToken对象，包含用户详细信息和权限。
     * @throws AuthenticationException 如果认证失败，抛出此异常。
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 从认证信息中提取用户名和密码
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        // 根据用户名加载用户详细信息
        UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);

        // 验证密码是否匹配
        boolean matches = myPasswordEncoder.passwordEncoder().matches(password, userDetails.getPassword());

        // 如果密码不匹配，抛出认证异常
        if (!matches){
            throw new AuthenticationException("用户名或密码错误") {};
        }

        // 如果密码匹配，创建并返回新的认证令牌
        return new UsernamePasswordAuthenticationToken(userDetails,userDetails.getPassword(),userDetails.getAuthorities());
    }


    /**
     * 判断当前Security框架的认证机制是否支持指定的认证类。
     * 本方法特定于判断是否支持UsernamePasswordAuthenticationToken类型的认证。
     *
     * @param authentication 需要验证其是否受支持的认证类。
     * @return 如果指定的认证类是UsernamePasswordAuthenticationToken的实例或其子类，则返回true；否则返回false。
     */
    @Override
    public boolean supports(Class<?> authentication) {
        // 判断传入的认证类是否可以被UsernamePasswordAuthenticationToken类所派生
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
