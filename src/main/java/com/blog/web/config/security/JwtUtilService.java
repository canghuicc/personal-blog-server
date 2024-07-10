package com.blog.web.config.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.web.entity.User;
import com.blog.web.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author 苍晖
 * @since 2024/7/7 下午4:55
 */
@Service
public class JwtUtilService {

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private SecretKey KEY;

    private final static SecureDigestAlgorithm<SecretKey, SecretKey> ALGORITHM = Jwts.SIG.HS256;

    @PostConstruct
    public void init() {
        this.KEY = Keys.hmacShaKeyFor(secret.getBytes());
    }

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
                .header()
                .add("typ", "JWT")
                .add("alg", "HS256")
                .and()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(instance.getTime())
                .signWith(KEY, ALGORITHM)
                .compact();

        redisTemplate.opsForValue().set("token:" + username, token, 30, TimeUnit.MINUTES);
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
            return Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
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
            Claims claims = Jwts.parser().verifyWith(KEY).build().parseSignedClaims(token).getPayload();

            // 检查令牌的过期时间是否早于当前时间
            return claims.getExpiration().before(new Date());
        } catch (SignatureException e) {
            // 如果签名无效，说明令牌已过期或被篡改，返回true
            return true;
        }
    }

    /**
     * 刷新JWT令牌。
     * <p>
     * 如果当前令牌接近过期，生成新的JWT令牌。
     *
     * @param token   当前的JWT令牌。
     * @param minutes 令牌剩余有效时间的最小分钟数，低于此值则刷新。
     * @return 新的JWT令牌或原始令牌，取决于是否需要刷新。
     */
    public String refreshToken(String token, int minutes) {
        Claims claims = parseToken(token);
        if (claims != null && getMinutesUntilExpiration(claims) <= minutes) {
            return createToken(claims.getSubject());
        }
        return token;
    }

    /**
     * 获取令牌距离过期的分钟数。
     *
     * @param claims 解析后的令牌声明。
     * @return 距离过期的时间（分钟）。
     */
    private int getMinutesUntilExpiration(Claims claims) {
        Date expiration = claims.getExpiration();
        long timeLeftMillis = expiration.getTime() - System.currentTimeMillis();
        return (int) (timeLeftMillis / (1000 * 60));
    }

    /**
     * 验证令牌（token）的有效性。
     * 通过解析令牌获取用户名，并验证数据库中是否存在对应的用户。
     *
     * @param token 待验证的令牌字符串。
     * @return 如果令牌有效（即对应一个存在的用户名），返回true；否则返回false。
     */
    public boolean validateToken(String token) {
        // 从令牌中提取用户名
        final String username = extractUsernameFromToken(token);
        if (username != null) {
            // 根据用户名查询数据库中的用户信息
            User user=userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
            // 如果查询结果不为空，则表示用户名存在，令牌有效
            return user != null;
        }
        // 如果用户名为空，直接返回无效
        return false;
    }


    /**
     * 从JWT令牌中提取用户名。
     *
     * @param token 待解析的JWT令牌。
     * @return 如果解析成功，返回用户名；如果解析失败，返回null。
     */
    public String extractUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

}
