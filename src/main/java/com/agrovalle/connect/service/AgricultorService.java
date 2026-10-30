
package com.agrovalle.connect.service;

import com.agrovalle.connect.dto.AgricultorRegistroRequest;
import com.agrovalle.connect.dto.AgricultorRegistroResponse;
import com.agrovalle.connect.exception.CorreoDuplicadoException;
import com.agrovalle.connect.exception.IdentificacionDuplicadaException;
import com.agrovalle.connect.model.Agricultor;
import com.agrovalle.connect.model.Usuario;
import com.agrovalle.connect.model.enums.Rol;
import com.agrovalle.connect.repository.AgricultorRepository;
import com.agrovalle.connect.repository.UsuarioRepository;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de negocio para el registro de agricultores. Si la identificación
 * ya existe como Usuario (porque la persona ya tiene cuenta con otro rol,
 * por ejemplo comprador), reutiliza esa cuenta y le agrega el rol
 * AGRICULTOR, en vez de crear una cuenta nueva. Si la identificación es
 * nueva, crea primero la cuenta de autenticación compartida (Usuario) y
 * luego el perfil de agricultor enlazado a ella.
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
   * Registra un nuevo agricultor. Si ya existe un Usuario con esa
   * identificación y aún no tiene el rol AGRICULTOR, reutiliza esa cuenta
   * (agregándole el rol) en vez de crear una nueva; si ya es agricultor,
   * rechaza el registro como duplicado. Crea el perfil en Agricultor
   * enlazado a la cuenta dentro de la misma transacción.
   */
  @Transactional
  public AgricultorRegistroResponse registrar(AgricultorRegistroRequest request) {
    Optional<Usuario> usuarioExistente =
        usuarioRepository.findByIdentificacion(request.identificacion());

    Usuario usuario;
    if (usuarioExistente.isPresent()) {
      usuario = usuarioExistente.get();
      if (usuario.tieneRol(Rol.AGRICULTOR)) {
        throw new IdentificacionDuplicadaException(request.identificacion());
      }
      usuario.agregarRol(Rol.AGRICULTOR);
      usuarioRepository.save(usuario);
    } else {
      if (usuarioRepository.existsByCorreo(request.correo())) {
        throw new CorreoDuplicadoException(request.correo());
      }
      Usuario nuevoUsuario = new Usuario(
          request.identificacion(),
          request.nombre(),
          request.apellido(),
          request.telefono(),
          request.correo(),
          passwordEncoder.encode(request.contrasena()),
          Rol.AGRICULTOR);
      usuario = usuarioRepository.save(nuevoUsuario);
    }

    Agricultor agricultor = new Agricultor(request.municipio(), usuario);

    Agricultor guardado = agricultorRepository.save(agricultor);

    return new AgricultorRegistroResponse(
        guardado.getId(),
        usuario.getNombre(),
        usuario.getApellido(),
        usuario.getIdentificacion(),
        guardado.getMunicipio(),
        "Registro realizado correctamente");
  }
}