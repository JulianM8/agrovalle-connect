package com.agrovalle.connect.service;

import com.agrovalle.connect.model.enums.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio para generar y validar tokens JWT. El token identifica al
 * Usuario (cuenta de autenticación compartida) y lleva su rol como claim,
 * para que cualquier perfil (agricultor, comprador, transportista) se
 * autentique de la misma forma.
 */
@Service
public class JwtService {

  private final Key clave;

  private final long expiracionMs;

  public JwtService(@Value("${jwt.secret}") String secreto,
      @Value("${jwt.expiration-ms}") long expiracionMs) {
    this.clave = Keys.hmacShaKeyFor(secreto.getBytes());
    this.expiracionMs = expiracionMs;
  }

  public String generarToken(Long usuarioId, Rol rol) {
    Date ahora = new Date();
    Date expiracion = new Date(ahora.getTime() + expiracionMs);
    return Jwts.builder()
        .subject(usuarioId.toString())
        .claim("rol", rol.name())
        .issuedAt(ahora)
        .expiration(expiracion)
        .signWith(clave)
        .compact();
  }

  public long getExpiracionMs() {
    return expiracionMs;
  }

  public Long extraerUsuarioId(String token) {
    return Long.valueOf(parseClaims(token).getSubject());
  }

  public String extraerRol(String token) {
    return parseClaims(token).get("rol", String.class);
  }

  private Claims parseClaims(String token) {
    return Jwts.parser().verifyWith((javax.crypto.SecretKey) clave)
        .build().parseSignedClaims(token).getPayload();
  }
}
