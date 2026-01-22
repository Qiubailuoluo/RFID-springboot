package com.zebrarfid.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private long expire;

    /**
     * 生成JWT令牌
     */
    public String generateToken(UserDetails userDetails) {
        // 自定义载荷（存储用户账号）
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userDetails.getUsername());
        return Jwts.builder()
                .setClaims(claims) // 载荷
                .setSubject(userDetails.getUsername()) // 主题（用户账号）
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expire)) // 过期时间
                .signWith(SignatureAlgorithm.HS256, secret) // 签名算法+密钥
                .compact();
    }

    /**
     * 解析JWT令牌，获取载荷
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("JWT解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 校验JWT是否过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration().before(new Date());
    }

    /**
     * 校验JWT有效性
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return false;
        }
        // 校验账号匹配 + 未过期
        String username = claims.getSubject();
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}
