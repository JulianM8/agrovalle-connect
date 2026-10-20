
package com.agrovalle.connect.service;

import com.agrovalle.connect.dto.AgricultorRegistroRequest;
import com.agrovalle.connect.dto.AgricultorRegistroResponse;
import com.agrovalle.connect.exception.IdentificacionDuplicadaException;
import com.agrovalle.connect.model.Agricultor;
import com.agrovalle.connect.model.Usuario;
import com.agrovalle.connect.model.enums.Rol;
import com.agrovalle.connect.repository.AgricultorRepository;
import com.agrovalle.connect.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de negocio para el registro de agricultores. Crea primero la
 * cuenta de autenticación compartida (Usuario) y luego el perfil de
 * agricultor enlazado a ella.
 */
@Service
public class AgricultorService {

  private final AgricultorRepository agricultorRepository;

  private final UsuarioRepository usuarioRepository;

  private final PasswordEncoder passwordEncoder;

  public AgricultorService(AgricultorRepository agricultorRepository,
      UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
    this.agricultorRepository = agricultorRepository;
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Registra un nuevo agricultor, validando que su identificación no exista.
   * Crea la cuenta en Usuario (rol AGRICULTOR) y el perfil en Agricultor
   * dentro de la misma transacción.
   */
  @Transactional
  public AgricultorRegistroResponse registrar(AgricultorRegistroRequest request) {
    if (usuarioRepository.existsByIdentificacion(request.identificacion())) {
      throw new IdentificacionDuplicadaException(request.identificacion());
    }

    Usuario usuario = new Usuario(
        request.identificacion(),
        request.correo(),
        passwordEncoder.encode(request.contrasena()),
        Rol.AGRICULTOR);
    Usuario usuarioGuardado = usuarioRepository.save(usuario);

    Agricultor agricultor = new Agricultor(
        request.nombre(),
        request.apellido(),
        request.municipio(),
        request.telefono(),
        usuarioGuardado);

    Agricultor guardado = agricultorRepository.save(agricultor);

    return new AgricultorRegistroResponse(
        guardado.getId(),
        guardado.getNombre(),
        guardado.getApellido(),
        usuarioGuardado.getIdentificacion(),
        guardado.getMunicipio(),
        "Registro realizado correctamente");
  }
}