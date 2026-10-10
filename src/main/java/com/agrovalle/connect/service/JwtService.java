package com.agrovalle.connect.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final Key clave;
  
  private final long expiracionMs;

  public JwtService(@Value("${jwt.secret}") String secreto,
      @Value("${jwt.expiration-ms}") long expiracionMs) {
    this.clave = Keys.hmacShaKeyFor(secreto.getBytes());
    this.expiracionMs = expiracionMs;
  }

  public String generarToken(Long agricultorId) {
    Date ahora = new Date();
    Date expiracion = new Date(ahora.getTime() + expiracionMs);
    return Jwts.builder()
        .subject(agricultorId.toString())
        .issuedAt(ahora)
        .expiration(expiracion)
        .signWith(clave)
        .compact();
  }

  public long getExpiracionMs() {
    return expiracionMs;
  }

  public Long extraerAgricultorId(String token) {
    Claims claims = Jwts.parser().verifyWith((javax.crypto.SecretKey) clave)
        .build().parseSignedClaims(token).getPayload();
    return Long.valueOf(claims.getSubject());
  }
}
