package com.spacework.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret:SPACEWORK_SECRET_KEY_2026_MIN_256_BITS_SECURE}")
    private String jwtSecret = "SPACEWORK_SECRET_KEY_2026_MIN_256_BITS_SECURE";

    @Value("${jwt.expiration-ms:3600000}")
    private long jwtExpirationMs = 3600000L;

    public String generarToken(String username, String nombre, String email, String rol) {
        return Jwts.builder()
                .setSubject(username)
                .claim("nombre", nombre)
                .claim("email", email)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String obtenerUsuario(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    public Claims obtenerClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            return null;
        }
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
