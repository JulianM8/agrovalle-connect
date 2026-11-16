package com.agrovalle.connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class TransportistaServiceTest {

  @Mock
  private TransportistaRepository transportistaRepository;

  @Mock
  private UsuarioRepository usuarioRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private TransportistaService transportistaService;

  private TransportistaRegistroRequest crearRequest() {
    return new TransportistaRegistroRequest(
        "Mateo", "Rios", "1111078902", "3001234567", "mateo@correo.com", "contrasena123",
        "Palmira");
  }

  private Usuario crearUsuarioConRol(Rol rol) {
    Usuario usuario = new Usuario(
        "1111078902", "Mateo", "Rios", "3001234567", "mateo@correo.com",
        "hash-existente", rol);
    usuario.setId(5L);
    return usuario;
  }

  @Test
  void debeRegistrarTransportistaConCuentaNueva() {
    when(usuarioRepository.findByIdentificacion("1111078902")).thenReturn(Optional.empty());
    when(usuarioRepository.existsByCorreo("mateo@correo.com")).thenReturn(false);
    when(passwordEncoder.encode("contrasena123")).thenReturn("hash-simulado");
    when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> {
      Usuario usuario = invocacion.getArgument(0);
      usuario.setId(1L);
      return usuario;
    });
    when(transportistaRepository.save(any(Transportista.class))).thenAnswer(invocacion -> {
      Transportista transportista = invocacion.getArgument(0);
      transportista.setId(1L);
      return transportista;
    });

    TransportistaRegistroResponse respuesta = transportistaService.registrar(crearRequest());

    assertEquals(1L, respuesta.id());
    assertEquals("Mateo", respuesta.nombre());
    assertEquals("Palmira", respuesta.municipio());
    assertEquals("Registro realizado correctamente", respuesta.mensaje());
    verify(passwordEncoder).encode("contrasena123");
    verify(usuarioRepository).save(any(Usuario.class));
    verify(transportistaRepository).save(any(Transportista.class));
  }

  @Test
  void debeReutilizarCuentaExistenteYAgregarRolTransportista() {
    Usuario usuarioComprador = crearUsuarioConRol(Rol.COMPRADOR);
    when(usuarioRepository.findByIdentificacion("1111078902"))
        .thenReturn(Optional.of(usuarioComprador));
    when(passwordEncoder.matches("contrasena123", "hash-existente")).thenReturn(true);
    when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    when(transportistaRepository.save(any(Transportista.class))).thenAnswer(invocacion -> {
      Transportista transportista = invocacion.getArgument(0);
      transportista.setId(2L);
      return transportista;
    });

    TransportistaRegistroResponse respuesta = transportistaService.registrar(crearRequest());

    assertTrue(usuarioComprador.tieneRol(Rol.TRANSPORTISTA));
    assertTrue(usuarioComprador.tieneRol(Rol.COMPRADOR));
    assertEquals(2L, respuesta.id());
    verify(usuarioRepository).save(usuarioComprador);
    verify(usuarioRepository, never()).existsByCorreo(anyString());
    verify(passwordEncoder, never()).encode(anyString());
  }

  @Test
  void debeRechazarContrasenaIncorrectaAlReutilizarCuenta() {
    Usuario usuarioComprador = crearUsuarioConRol(Rol.COMPRADOR);
    when(usuarioRepository.findByIdentificacion("1111078902"))
        .thenReturn(Optional.of(usuarioComprador));
    when(passwordEncoder.matches("contrasena123", "hash-existente")).thenReturn(false);

    assertThrows(CredencialesInvalidasException.class,
        () -> transportistaService.registrar(crearRequest()));

    assertFalse(usuarioComprador.tieneRol(Rol.TRANSPORTISTA));
    verify(usuarioRepository, never()).save(any(Usuario.class));
    verify(transportistaRepository, never()).save(any(Transportista.class));
  }

  @Test
  void debeRechazarIdentificacionDuplicadaSiYaEsTransportista() {
    Usuario usuarioTransportista = crearUsuarioConRol(Rol.TRANSPORTISTA);
    when(usuarioRepository.findByIdentificacion("1111078902"))
        .thenReturn(Optional.of(usuarioTransportista));
    when(passwordEncoder.matches("contrasena123", "hash-existente")).thenReturn(true);

    assertThrows(IdentificacionDuplicadaException.class,
        () -> transportistaService.registrar(crearRequest()));

    verify(usuarioRepository, never()).save(any(Usuario.class));
    verify(transportistaRepository, never()).save(any(Transportista.class));
  }

  @Test
  void debeRechazarCorreoDuplicadoAlCrearUsuarioNuevo() {
    when(usuarioRepository.findByIdentificacion("1111078902")).thenReturn(Optional.empty());
    when(usuarioRepository.existsByCorreo("mateo@correo.com")).thenReturn(true);

    assertThrows(CorreoDuplicadoException.class,
        () -> transportistaService.registrar(crearRequest()));

    verify(usuarioRepository, never()).save(any(Usuario.class));
    verify(transportistaRepository, never()).save(any(Transportista.class));
  }
}