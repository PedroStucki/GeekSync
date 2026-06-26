package br.com.geeksync.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilitário responsável por gerar, validar e extrair informações dos tokens JWT.
 * Usado pelo AuthController para gerar o token no login
 * e pelo JwtFilter para validar o token em cada requisição.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gera um token JWT com userId, username e perfil no payload.
     */
    public String gerarToken(Long userId, String username, String perfil) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("perfil", perfil)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    /**
     * Extrai o username (subject) do token.
     */
    public String extrairUsername(String token) {
        return extrairClaims(token).getSubject();
    }

    /**
     * Extrai o perfil do usuário do token.
     */
    public String extrairPerfil(String token) {
        return extrairClaims(token).get("perfil", String.class);
    }

    /**
     * Verifica se o token é válido e não expirou.
     */
    public boolean validarToken(String token) {
        try {
            extrairClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}