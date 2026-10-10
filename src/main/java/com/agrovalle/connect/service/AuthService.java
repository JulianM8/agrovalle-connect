package com.agrovalle.connect.service;

import com.agrovalle.connect.dto.LoginRequest;
import com.agrovalle.connect.dto.LoginResponse;
import com.agrovalle.connect.exception.CredencialesInvalidasException;
import com.agrovalle.connect.repository.AgricultorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final AgricultorRepository agricultorRepository;

  private final PasswordEncoder passwordEncoder;
  
  private final JwtService jwtService;

  public AuthService(AgricultorRepository agricultorRepository,
      PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.agricultorRepository = agricultorRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public LoginResponse login(LoginRequest request) {
    var agricultor = agricultorRepository.findByIdentificacion(request.identificacion())
        .orElseThrow(CredencialesInvalidasException::new);

    if (!passwordEncoder.matches(request.contrasena(), agricultor.getContrasena())) {
      throw new CredencialesInvalidasException();
    }

    String token = jwtService.generarToken(agricultor.getId());
    return new LoginResponse(token, "Bearer", jwtService.getExpiracionMs());
  }
}
