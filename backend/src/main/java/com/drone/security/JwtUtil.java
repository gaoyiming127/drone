package com.drone.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 令牌工具：负责令牌的签发、解析与有效性校验。
 *
 * <p>密钥与有效期来自配置项 jwt.secret / jwt.expiration；令牌以用户名为主题，
 * 并携带角色声明，但鉴权时以数据库中的角色为准，不直接采信令牌内容。</p>
 */
@Component
public class JwtUtil {

    /** 用于签名与验签的 HMAC 密钥，由配置的密钥字符串派生 */
    private final SecretKey secretKey;

    /** 令牌有效期（毫秒） */
    private final long expiration;

    /**
     * 依据配置构造令牌工具。
     *
     * @param secret     签名密钥字符串，长度须满足 HMAC-SHA 的最小要求
     * @param expiration 令牌有效期（毫秒）
     */
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * 签发令牌。
     *
     * @param username 登录名，作为令牌主题
     * @param role     用户角色，作为 role 声明写入
     * @return 签名后的 JWT 字符串
     */
    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 从令牌中解析登录名。
     *
     * @param token JWT 字符串
     * @return 令牌主题（登录名）
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 从令牌中解析角色声明。
     *
     * <p>【待人工确认】当前项目内未发现调用（鉴权时角色取自数据库），
     * 因属于令牌工具的对外方法，暂予保留，请人工确认后再决定是否删除。</p>
     *
     * @param token JWT 字符串
     * @return 令牌中的角色编码
     */
    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * 校验令牌是否有效（签名正确且未过期）。
     *
     * @param token JWT 字符串
     * @return 有效返回 true；签名错误、格式非法或已过期返回 false
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 使用密钥验签并解析令牌载荷。
     *
     * @param token JWT 字符串
     * @return 令牌载荷
     * @throws JwtException 签名不合法或令牌已过期时抛出
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
