package com.agrovalle.connect.service;

import com.agrovalle.connect.dto.CompradorRegistroRequest;
import com.agrovalle.connect.dto.CompradorRegistroResponse;
import com.agrovalle.connect.exception.CorreoDuplicadoException;
import com.agrovalle.connect.exception.CredencialesInvalidasException;
import com.agrovalle.connect.exception.IdentificacionDuplicadaException;
import com.agrovalle.connect.model.Comprador;
import com.agrovalle.connect.model.Usuario;
import com.agrovalle.connect.model.enums.Rol;
import com.agrovalle.connect.repository.CompradorRepository;
import com.agrovalle.connect.repository.UsuarioRepository;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de negocio para el registro de compradores. Si la identificación ya
 * existe como Usuario (la persona ya tiene cuenta con otro rol), reutiliza esa
 * cuenta y le agrega el rol COMPRADOR, siempre que la contraseña enviada
 * coincida con la de la cuenta. Si la identificación es nueva, crea primero el
 * Usuario y luego el perfil de comprador enlazado a él.
 */
@Service
public class CompradorService {

  private final CompradorRepository compradorRepository;

  private final UsuarioRepository usuarioRepository;

  private final PasswordEncoder passwordEncoder;

  public CompradorService(CompradorRepository compradorRepository,
      UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
    this.compradorRepository = compradorRepository;
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Registra un nuevo comprador. Si ya existe un Usuario con esa identificación,
   * valida su contraseña y le agrega el rol COMPRADOR; si ya era comprador,
   * rechaza el registro como duplicado.
   */
  @Transactional
  public CompradorRegistroResponse registrar(CompradorRegistroRequest request) {
    Optional<Usuario> usuarioExistente =
        usuarioRepository.findByIdentificacion(request.identificacion());

    Usuario usuario;
    if (usuarioExistente.isPresent()) {
      usuario = usuarioExistente.get();
      if (!passwordEncoder.matches(request.contrasena(), usuario.getContrasena())) {
        throw new CredencialesInvalidasException();
      }
      if (usuario.tieneRol(Rol.COMPRADOR)) {
        throw new IdentificacionDuplicadaException(request.identificacion());
      }
      usuario.agregarRol(Rol.COMPRADOR);
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
          Rol.COMPRADOR);
      usuario = usuarioRepository.save(nuevoUsuario);
    }

    Comprador comprador = new Comprador(
        request.municipio(),
        request.direccionEntrega(),
        request.latitud(),
        request.longitud(),
        usuario);
    Comprador guardado = compradorRepository.save(comprador);

    return new CompradorRegistroResponse(
        guardado.getId(),
        usuario.getNombre(),
        usuario.getApellido(),
        usuario.getIdentificacion(),
        guardado.getMunicipio(),
        guardado.getDireccionEntrega(),
        "Registro realizado correctamente");
  }
}
