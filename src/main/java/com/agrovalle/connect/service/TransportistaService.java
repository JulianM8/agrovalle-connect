package com.agrovalle.connect.service;

import com.agrovalle.connect.dto.TransportistaRegistroRequest;
import com.agrovalle.connect.dto.TransportistaRegistroResponse;
import com.agrovalle.connect.exception.CorreoDuplicadoException;
import com.agrovalle.connect.exception.CredencialesInvalidasException;
import com.agrovalle.connect.exception.IdentificacionDuplicadaException;
import com.agrovalle.connect.model.Transportista;
import com.agrovalle.connect.model.Usuario;
import com.agrovalle.connect.model.enums.Rol;
import com.agrovalle.connect.repository.TransportistaRepository;
import com.agrovalle.connect.repository.UsuarioRepository;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de negocio para el registro de transportistas. Si la identificación
 * ya existe como Usuario (la persona ya tiene cuenta con otro rol), reutiliza
 * esa cuenta y le agrega el rol TRANSPORTISTA, siempre que la contraseña
 * enviada coincida con la de la cuenta. Si la identificación es nueva, crea
 * primero el Usuario y luego el perfil de transportista enlazado a él.
 */
@Service
public class TransportistaService {

  private final TransportistaRepository transportistaRepository;

  private final UsuarioRepository usuarioRepository;

  private final PasswordEncoder passwordEncoder;

  public TransportistaService(TransportistaRepository transportistaRepository,
      UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
    this.transportistaRepository = transportistaRepository;
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Registra un nuevo transportista. Si ya existe un Usuario con esa
   * identificación, valida su contraseña y le agrega el rol TRANSPORTISTA; si
   * ya era transportista, rechaza el registro como duplicado.
   */
  @Transactional
  public TransportistaRegistroResponse registrar(TransportistaRegistroRequest request) {
    Optional<Usuario> usuarioExistente =
        usuarioRepository.findByIdentificacion(request.identificacion());

    Usuario usuario;
    if (usuarioExistente.isPresent()) {
      usuario = usuarioExistente.get();
      if (!passwordEncoder.matches(request.contrasena(), usuario.getContrasena())) {
        throw new CredencialesInvalidasException();
      }
      if (usuario.tieneRol(Rol.TRANSPORTISTA)) {
        throw new IdentificacionDuplicadaException(request.identificacion());
      }
      usuario.agregarRol(Rol.TRANSPORTISTA);
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
          Rol.TRANSPORTISTA);
      usuario = usuarioRepository.save(nuevoUsuario);
    }

    Transportista transportista = new Transportista(request.municipio(), usuario);
    Transportista guardado = transportistaRepository.save(transportista);

    return new TransportistaRegistroResponse(
        guardado.getId(),
        usuario.getNombre(),
        usuario.getApellido(),
        usuario.getIdentificacion(),
        guardado.getMunicipio(),
        "Registro realizado correctamente");
  }
}
