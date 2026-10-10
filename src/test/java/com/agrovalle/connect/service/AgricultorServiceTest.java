package com.agrovalle.connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agrovalle.connect.dto.AgricultorRegistroRequest;
import com.agrovalle.connect.dto.AgricultorRegistroResponse;
import com.agrovalle.connect.exception.IdentificacionDuplicadaException;
import com.agrovalle.connect.model.Agricultor;
import com.agrovalle.connect.repository.AgricultorRepository;
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
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private AgricultorService agricultorService;

  private AgricultorRegistroRequest crearRequest() {
    return new AgricultorRegistroRequest(
        "Juan", "Perez", "1111078900", "Palmira", "3001234567", "juan@correo.com", "contrasena123");
  }

  @Test
  void debeRegistrarAgricultorConDatosValidos() {
    when(agricultorRepository.existsByIdentificacion("1111078900")).thenReturn(false);
    when(passwordEncoder.encode("contrasena123")).thenReturn("hash-simulado");
    when(agricultorRepository.save(any(Agricultor.class))).thenAnswer(invocacion -> {
      Agricultor agricultor = invocacion.getArgument(0);
      agricultor.setId(1L);
      return agricultor;
    });

    AgricultorRegistroResponse respuesta = agricultorService.registrar(crearRequest());

    assertEquals(1L, respuesta.id());
    assertEquals("Juan", respuesta.nombre());
    assertEquals("Registro realizado correctamente", respuesta.mensaje());
    verify(passwordEncoder).encode("contrasena123");
    verify(agricultorRepository).save(any(Agricultor.class));
  }

  @Test
  void debeGuardarLaContrasenaCifradaNoEnTextoPlano() {
    when(agricultorRepository.existsByIdentificacion(anyString())).thenReturn(false);
    when(passwordEncoder.encode("contrasena123")).thenReturn("hash-simulado");
    when(agricultorRepository.save(any(Agricultor.class))).thenAnswer(invocacion -> {
      Agricultor agricultor = invocacion.getArgument(0);
      assertEquals("hash-simulado", agricultor.getContrasena());
      agricultor.setId(1L);
      return agricultor;
    });

    agricultorService.registrar(crearRequest());

    verify(agricultorRepository).save(any(Agricultor.class));
  }

  @Test
  void debeRechazarIdentificacionDuplicada() {
    when(agricultorRepository.existsByIdentificacion("1111078900")).thenReturn(true);

    assertThrows(IdentificacionDuplicadaException.class,
        () -> agricultorService.registrar(crearRequest()));

    verify(agricultorRepository, never()).save(any(Agricultor.class));
    verify(passwordEncoder, never()).encode(anyString());
  }
}