package com.blog.web.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

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

    public String createToken(String username) {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MINUTE, 30);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(instance.getTime())
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public boolean isExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token).getBody();
            return claims.getExpiration().before(new Date());
        } catch (SignatureException e) {
            return true;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Any exception indicates an invalid token.
            return false;
        }
    }
}
