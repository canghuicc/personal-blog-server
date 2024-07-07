package com.blog.web.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * JWT Utility Service.
 *
 * @author 苍晖
 * @since 2024/7/7 下午4:55
 */
@Service
public class JwtUtilService {

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 创建一个JWT令牌。
     * <p>
     * 该方法用于生成一个针对指定用户名的JSON Web Token（JWT）。JWT包含用户名信息，
     * 并设置令牌的发行时间和过期时间，确保令牌在30分钟内有效。
     * 使用HS256算法对令牌进行签名，确保令牌的完整性和安全性。
     *
     * @param username 令牌的主题，即用户名。
     * @return 生成的JWT令牌字符串。
     */
    public String createToken(String username) {
        // 获取当前时间并添加30分钟，设置令牌的过期时间
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MINUTE, 30);

        // 使用JWT库的builder模式构建令牌
        // 设置主题（用户名）、发行时间、过期时间，并使用预定义的私钥对令牌进行签名
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(instance.getTime())
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256, secret)
                .compact();

        redisTemplate.opsForValue().set("token:" + token, username, 30, TimeUnit.MINUTES);
        return token;
    }

    /**
     * 解析JWT令牌并返回其中的声明。
     *
     * @param token 待解析的JWT令牌。
     * @return 如果解析成功，返回包含声明的Claims对象；如果解析失败，返回null。
     */
    public Claims parseToken(String token) {
        try {
            // 使用JJWT库创建解析器构建器，设置签名密钥为预定义的secret，然后构建解析器。
            // 使用解析器解析JWT令牌并获取Claims部分。
            return Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // 解析失败时，捕获异常并返回null。
            return null;
        }
    }


    /**
     * 检查令牌是否过期。
     * <p>
     * 本方法通过解析JWT令牌来获取其中的过期时间，并与当前时间进行比较，以确定令牌是否过期。
     * 如果令牌签名无效，将视为令牌过期。
     *
     * @param token 待检查的JWT令牌字符串。
     * @return 如果令牌过期或签名无效，则返回true；否则返回false。
     */
    public boolean isExpired(String token) {
        try {
            // 解析JWT令牌，获取其中的声明（Claims）
            Claims claims = Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token).getBody();

            // 检查令牌的过期时间是否早于当前时间
            return claims.getExpiration().before(new Date());
        } catch (SignatureException e) {
            // 如果签名无效，说明令牌已过期或被篡改，返回true
            return true;
        }
    }


    /**
     * 验证JWT令牌的有效性。
     * 使用预设的密钥对令牌进行解析，如果解析成功，则令牌有效；反之，令牌无效。
     *
     * @param token 待验证的JWT令牌字符串。
     * @return 如果令牌有效，则返回true；否则返回false。
     */
    public boolean validateToken(String token) {
        try {
            // 使用JWT的解析器，并设置签名密钥，尝试解析令牌。
            Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 解析失败，捕获到异常，说明令牌无效。
            return false;
        }
    }

}
