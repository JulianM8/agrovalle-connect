package com.agrovalle.connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CompradorServiceTest {

  @Mock
  private CompradorRepository compradorRepository;

  @Mock
  private UsuarioRepository usuarioRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private CompradorService compradorService;

  private CompradorRegistroRequest crearRequest() {
    return new CompradorRegistroRequest(
        "Sofia", "Torres", "1111078901", "3001234567", "sofia@correo.com", "contrasena123",
        "Cali", "Calle 5 # 10-20", new BigDecimal("3.451647"), new BigDecimal("-76.531985"));
  }

  private Usuario crearUsuarioAgricultor() {
    Usuario usuario = new Usuario(
        "1111078901", "Sofia", "Torres", "3001234567", "sofia@correo.com",
        "hash-existente", Rol.AGRICULTOR);
    usuario.setId(5L);
    return usuario;
  }

  @Test
  void debeRegistrarCompradorConCuentaNueva() {
    when(usuarioRepository.findByIdentificacion("1111078901")).thenReturn(Optional.empty());
    when(usuarioRepository.existsByCorreo("sofia@correo.com")).thenReturn(false);
    when(passwordEncoder.encode("contrasena123")).thenReturn("hash-simulado");
    when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> {
      Usuario usuario = invocacion.getArgument(0);
      usuario.setId(1L);
      return usuario;
    });
    when(compradorRepository.save(any(Comprador.class))).thenAnswer(invocacion -> {
      Comprador comprador = invocacion.getArgument(0);
      comprador.setId(1L);
      return comprador;
    });

    CompradorRegistroResponse respuesta = compradorService.registrar(crearRequest());

    assertEquals(1L, respuesta.id());
    assertEquals("Sofia", respuesta.nombre());
    assertEquals("Cali", respuesta.municipio());
    assertEquals("Registro realizado correctamente", respuesta.mensaje());
    verify(passwordEncoder).encode("contrasena123");
    verify(usuarioRepository).save(any(Usuario.class));
    verify(compradorRepository).save(any(Comprador.class));
  }

  @Test
  void debeReutilizarCuentaExistenteYAgregarRolComprador() {
    Usuario usuarioAgricultor = crearUsuarioAgricultor();
    when(usuarioRepository.findByIdentificacion("1111078901"))
        .thenReturn(Optional.of(usuarioAgricultor));
    when(passwordEncoder.matches("contrasena123", "hash-existente")).thenReturn(true);
    when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    when(compradorRepository.save(any(Comprador.class))).thenAnswer(invocacion -> {
      Comprador comprador = invocacion.getArgument(0);
      comprador.setId(2L);
      return comprador;
    });

    CompradorRegistroResponse respuesta = compradorService.registrar(crearRequest());

    assertTrue(usuarioAgricultor.tieneRol(Rol.COMPRADOR));
    assertTrue(usuarioAgricultor.tieneRol(Rol.AGRICULTOR));
    assertEquals(2L, respuesta.id());
    verify(usuarioRepository).save(usuarioAgricultor);
    verify(usuarioRepository, never()).existsByCorreo(anyString());
    verify(passwordEncoder, never()).encode(anyString());
  }

  @Test
  void debeRechazarContrasenaIncorrectaAlReutilizarCuenta() {
    Usuario usuarioAgricultor = crearUsuarioAgricultor();
    when(usuarioRepository.findByIdentificacion("1111078901"))
        .thenReturn(Optional.of(usuarioAgricultor));
    when(passwordEncoder.matches("contrasena123", "hash-existente")).thenReturn(false);

    assertThrows(CredencialesInvalidasException.class,
        () -> compradorService.registrar(crearRequest()));

    assertTrue(!usuarioAgricultor.tieneRol(Rol.COMPRADOR));
    verify(usuarioRepository, never()).save(any(Usuario.class));
    verify(compradorRepository, never()).save(any(Comprador.class));
  }

  @Test
  void debeRechazarIdentificacionDuplicadaSiYaEsComprador() {
    Usuario usuarioComprador = new Usuario(
        "1111078901", "Sofia", "Torres", "3001234567", "sofia@correo.com",
        "hash-existente", Rol.COMPRADOR);
    usuarioComprador.setId(5L);
    when(usuarioRepository.findByIdentificacion("1111078901"))
        .thenReturn(Optional.of(usuarioComprador));
    when(passwordEncoder.matches("contrasena123", "hash-existente")).thenReturn(true);

    assertThrows(IdentificacionDuplicadaException.class,
        () -> compradorService.registrar(crearRequest()));

    verify(usuarioRepository, never()).save(any(Usuario.class));
    verify(compradorRepository, never()).save(any(Comprador.class));
  }

  @Test
  void debeRechazarCorreoDuplicadoAlCrearUsuarioNuevo() {
    when(usuarioRepository.findByIdentificacion("1111078901")).thenReturn(Optional.empty());
    when(usuarioRepository.existsByCorreo("sofia@correo.com")).thenReturn(true);

    assertThrows(CorreoDuplicadoException.class,
        () -> compradorService.registrar(crearRequest()));

    verify(usuarioRepository, never()).save(any(Usuario.class));
    verify(compradorRepository, never()).save(any(Comprador.class));
  }
}