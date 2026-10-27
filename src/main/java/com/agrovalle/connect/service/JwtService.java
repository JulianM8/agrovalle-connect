package com.agrovalle.connect.service;

import com.agrovalle.connect.model.enums.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio para generar y validar tokens JWT. El token identifica al
 * Usuario (cuenta de autenticación compartida) y lleva TODOS sus roles como
 * claim (un usuario puede ser agricultor y comprador a la vez, por
 * ejemplo), para que cualquier perfil se autentique de la misma forma.
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

  public String generarToken(Long usuarioId, Set<Rol> roles) {
    Date ahora = new Date();
    Date expiracion = new Date(ahora.getTime() + expiracionMs);
    List<String> nombresRoles = roles.stream().map(Rol::name).collect(Collectors.toList());
    return Jwts.builder()
        .subject(usuarioId.toString())
        .claim("roles", nombresRoles)
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

  @SuppressWarnings("unchecked")
  public List<String> extraerRoles(String token) {
    return parseClaims(token).get("roles", List.class);
  }

  private Claims parseClaims(String token) {
    return Jwts.parser().verifyWith((javax.crypto.SecretKey) clave)
        .build().parseSignedClaims(token).getPayload();
  }
}
