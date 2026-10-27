package com.agrovalle.connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AgricultorServiceTest {

  @Mock
  private AgricultorRepository agricultorRepository;

  @Mock
  private UsuarioRepository usuarioRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private AgricultorService agricultorService;

  private AgricultorRegistroRequest crearRequest() {
    return new AgricultorRegistroRequest(
        "Juan", "Perez", "1111078900", "Palmira", "3001234567", "juan@correo.com", "contrasena123");
  }

  @Test
  void debeRegistrarAgricultorConDatosValidos() {
    when(usuarioRepository.findByIdentificacion("1111078900")).thenReturn(Optional.empty());
    when(usuarioRepository.existsByCorreo("juan@correo.com")).thenReturn(false);
    when(passwordEncoder.encode("contrasena123")).thenReturn("hash-simulado");
    when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> {
      Usuario usuario = invocacion.getArgument(0);
      usuario.setId(1L);
      return usuario;
    });
    when(agricultorRepository.save(any(Agricultor.class))).thenAnswer(invocacion -> {
      Agricultor agricultor = invocacion.getArgument(0);
      agricultor.setId(1L);
      return agricultor;
    });

    AgricultorRegistroResponse respuesta = agricultorService.registrar(crearRequest());

    assertEquals(1L, respuesta.id());
    assertEquals("Juan", respuesta.nombre());
    assertEquals("1111078900", respuesta.identificacion());
    assertEquals("Registro realizado correctamente", respuesta.mensaje());
    verify(passwordEncoder).encode("contrasena123");
    verify(usuarioRepository).save(any(Usuario.class));
    verify(agricultorRepository).save(any(Agricultor.class));
  }

  @Test
  void debeGuardarLaContrasenaCifradaNoEnTextoPlano() {
    when(usuarioRepository.findByIdentificacion(anyString())).thenReturn(Optional.empty());
    when(usuarioRepository.existsByCorreo(anyString())).thenReturn(false);
    when(passwordEncoder.encode("contrasena123")).thenReturn("hash-simulado");
    when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> {
      Usuario usuario = invocacion.getArgument(0);
      assertEquals("hash-simulado", usuario.getContrasena());
      usuario.setId(1L);
      return usuario;
    });
    when(agricultorRepository.save(any(Agricultor.class))).thenAnswer(invocacion -> {
      Agricultor agricultor = invocacion.getArgument(0);
      agricultor.setId(1L);
      return agricultor;
    });

    agricultorService.registrar(crearRequest());

    verify(usuarioRepository).save(any(Usuario.class));
  }

  @Test
  void debeRechazarIdentificacionDuplicada() {
    Usuario usuarioExistente = new Usuario(
        "1111078900", "otro@correo.com", "hash-existente", Rol.AGRICULTOR);
    usuarioExistente.setId(5L);
    when(usuarioRepository.findByIdentificacion("1111078900"))
        .thenReturn(Optional.of(usuarioExistente));

    assertThrows(IdentificacionDuplicadaException.class,
        () -> agricultorService.registrar(crearRequest()));

    verify(usuarioRepository, never()).save(any(Usuario.class));
    verify(agricultorRepository, never()).save(any(Agricultor.class));
    verify(passwordEncoder, never()).encode(anyString());
  }

  @Test
  void debeRechazarCorreoDuplicadoAlCrearUsuarioNuevo() {
    when(usuarioRepository.findByIdentificacion("1111078900")).thenReturn(Optional.empty());
    when(usuarioRepository.existsByCorreo("juan@correo.com")).thenReturn(true);

    assertThrows(CorreoDuplicadoException.class,
        () -> agricultorService.registrar(crearRequest()));

    verify(usuarioRepository, never()).save(any(Usuario.class));
    verify(agricultorRepository, never()).save(any(Agricultor.class));
  }

  @Test
  void debeReutilizarUsuarioExistenteYAgregarRolAgricultorSiNoLoTenia() {
    Usuario usuarioComprador = new Usuario(
        "1111078900", "juan@correo.com", "hash-existente", Rol.COMPRADOR);
    usuarioComprador.setId(5L);
    when(usuarioRepository.findByIdentificacion("1111078900"))
        .thenReturn(Optional.of(usuarioComprador));
    when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    when(agricultorRepository.save(any(Agricultor.class))).thenAnswer(invocacion -> {
      Agricultor agricultor = invocacion.getArgument(0);
      agricultor.setId(1L);
      return agricultor;
    });

    AgricultorRegistroResponse respuesta = agricultorService.registrar(crearRequest());

    assertTrue(usuarioComprador.tieneRol(Rol.AGRICULTOR));
    assertTrue(usuarioComprador.tieneRol(Rol.COMPRADOR));
    assertEquals("Registro realizado correctamente", respuesta.mensaje());
    verify(usuarioRepository).save(usuarioComprador);
    verify(usuarioRepository, never()).existsByCorreo(anyString());
    verify(passwordEncoder, never()).encode(anyString());
  }
}