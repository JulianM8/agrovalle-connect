package com.agrovalle.connect.service;

import com.agrovalle.connect.dto.LoginRequest;
import com.agrovalle.connect.dto.LoginResponse;
import com.agrovalle.connect.exception.CredencialesInvalidasException;
import com.agrovalle.connect.model.Usuario;
import com.agrovalle.connect.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UsuarioRepository usuarioRepository;

  private final PasswordEncoder passwordEncoder;

  private final JwtService jwtService;

  public AuthService(UsuarioRepository usuarioRepository,
      PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  /**
   * Autentica a un usuario (agricultor, comprador o transportista) mediante
   * su identificación y contraseña. Si las credenciales son válidas y la
   * cuenta está activa, genera un token JWT con el id del usuario y su rol.
   */
  public LoginResponse login(LoginRequest request) {
    Usuario usuario = usuarioRepository.findByIdentificacion(request.identificacion())
        .orElseThrow(CredencialesInvalidasException::new);

    if (!usuario.isActivo()
        || !passwordEncoder.matches(request.contrasena(), usuario.getContrasena())) {
      throw new CredencialesInvalidasException();
    }

    String token = jwtService.generarToken(usuario.getId(), usuario.getRol());
    return new LoginResponse(token, "Bearer", jwtService.getExpiracionMs());
  }
}
