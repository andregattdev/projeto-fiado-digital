package com.app.Projeto.fiado.digital.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.app.Projeto.fiado.digital.config.JwtProperties;
import com.app.Projeto.fiado.digital.model.Comercio;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public String gerarToken(Comercio comercio) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + jwtProperties.getExpirationMs());

        return Jwts.builder()
                .subject(comercio.getEmail())
                .claim("comercioId", comercio.getId())
                .claim("nomeLoja", comercio.getNomeLoja())
                .claim("role", "COMERCIANTE")
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(getSigningKey())
                .compact();
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public Long extrairComercioId(String token) {
        return extrairClaims(token).get("comercioId", Long.class);
    }

    public boolean isTokenValido(String token, UserDetails userDetails) {
        String email = extrairEmail(token);
        return email.equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpirado(token);
    }

    private boolean isTokenExpirado(String token) {
        return extrairClaims(token).getExpiration().before(new Date());
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public long getExpirationMs() {
        return jwtProperties.getExpirationMs();
    }
}
