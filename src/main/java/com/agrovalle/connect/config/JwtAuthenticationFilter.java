package com.agrovalle.connect.config;

import com.agrovalle.connect.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  public JwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  /**
   * Filtra las solicitudes HTTP para autenticar usuarios (agricultores,
   * compradores o transportistas) mediante JWT. Si el token es válido, se
   * establece la autenticación en el contexto de seguridad con el id del
   * usuario como principal y una authority ROLE_&lt;rol&gt; por cada rol
   * que tenga el usuario (puede tener varios a la vez).
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String header = request.getHeader("Authorization");

    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      try {
        Long usuarioId = jwtService.extraerUsuarioId(token);
        List<String> roles = jwtService.extraerRoles(token);
        List<GrantedAuthority> authorities = roles != null
            ? roles.stream()
                .<GrantedAuthority>map(rol -> new SimpleGrantedAuthority("ROLE_" + rol))
                .toList()
            : List.of();
        var authentication = new UsernamePasswordAuthenticationToken(
            usuarioId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (Exception ex) {
        SecurityContextHolder.clearContext();
      }
    }

    filterChain.doFilter(request, response);
  }
}