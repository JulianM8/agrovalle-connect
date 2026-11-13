package com.agrovalle.connect.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class EstadoControllerTest {

  @Mock
  private DataSource dataSource;

  @Mock
  private Connection conexion;

  @Mock
  private Environment environment;

  @InjectMocks
  private EstadoController controller;

  @Test
  void estadoRetornaOkCuandoLaBaseDeDatosResponde() throws SQLException {
    when(environment.getActiveProfiles()).thenReturn(new String[] {"staging"});
    when(dataSource.getConnection()).thenReturn(conexion);
    when(conexion.isValid(2)).thenReturn(true);

    ResponseEntity<Map<String, String>> respuesta = controller.estado();

    assertEquals(HttpStatus.OK, respuesta.getStatusCode());
    assertEquals("OK", respuesta.getBody().get("estado"));
    assertEquals("staging", respuesta.getBody().get("perfil"));
  }

  @Test
  void estadoRetorna503CuandoLaBaseDeDatosFalla() throws SQLException {
    when(environment.getActiveProfiles()).thenReturn(new String[0]);
    when(dataSource.getConnection()).thenThrow(new SQLException("sin conexion"));

    ResponseEntity<Map<String, String>> respuesta = controller.estado();

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, respuesta.getStatusCode());
    assertEquals("ERROR", respuesta.getBody().get("estado"));
    assertEquals("default", respuesta.getBody().get("perfil"));
  }
}
