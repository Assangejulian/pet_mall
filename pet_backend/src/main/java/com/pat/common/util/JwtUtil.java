package com.pat.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
public class JwtUtil {

    private static final String DEFAULT_SECRET = "PetNest2024SecretKeyForJWTTokenGeneration!@#$";
    private static final String SECRET = System.getenv().getOrDefault("JWT_SECRET", DEFAULT_SECRET);
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public static final long ADMIN_EXPIRE = 2 * 3600 * 1000L;
    public static final long USER_EXPIRE = 7 * 24 * 3600 * 1000L;

    /**
     * 生成 JWT Token。
     *
     * @param userId 用户 ID
     * @param username 用户名
     * @param role 角色
     * @param expireMs 过期时间（毫秒）
     * @return JWT 字符串
     */
    public static String generateToken(Long userId, String username, String role, long expireMs) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireMs))
                .signWith(KEY)
                .compact();
    }

    /**
     * 解析 JWT Token。
     *
     * @param token JWT 字符串
     * @return Claims
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 校验 Token 有效性。
     *
     * @param token JWT 字符串
     * @return true 有效，false 无效
     */
    public static boolean validateToken(String token) {
        try { parseToken(token); return true; }
        catch (ExpiredJwtException e) { log.warn("JWT token expired"); return false; }
        catch (MalformedJwtException e) { log.warn("JWT token malformed: {}", e.getMessage()); return false; }
        catch (SignatureException e) { log.warn("JWT signature invalid: {}", e.getMessage()); return false; }
        catch (RuntimeException e) { log.error("JWT parse error", e); return false; }
    }
}