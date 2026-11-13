package com.agrovalle.connect.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone el estado de la aplicación y de su conexión a la base de datos.
 */
@RestController
@RequestMapping("/api/v1/estado")
public class EstadoController {

  private static final int TIMEOUT_SEGUNDOS = 2;

  private final DataSource dataSource;
  
  private final Environment environment;

  /**
   * Crea el controlador con sus dependencias.
   */
  public EstadoController(DataSource dataSource, Environment environment) {
    this.dataSource = dataSource;
    this.environment = environment;
  }

  /**
   * Retorna el estado de la aplicación, el perfil activo y la conexión a la base de datos.
   */
  @GetMapping
  public ResponseEntity<Map<String, String>> estado() {
    String[] perfiles = environment.getActiveProfiles();
    boolean baseConectada = baseDatosDisponible();

    Map<String, String> respuesta = new LinkedHashMap<>();
    respuesta.put("aplicacion", "AgroValle Connect");
    respuesta.put("estado", baseConectada ? "OK" : "ERROR");
    respuesta.put("perfil", perfiles.length > 0 ? perfiles[0] : "default");
    respuesta.put("baseDatos",
        baseConectada ? "PostgreSQL Connected" : "PostgreSQL Disconnected");

    HttpStatus codigo = baseConectada ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
    return ResponseEntity.status(codigo).body(respuesta);
  }

  private boolean baseDatosDisponible() {
    try (Connection conexion = dataSource.getConnection()) {
      return conexion.isValid(TIMEOUT_SEGUNDOS);
    } catch (SQLException e) {
      return false;
    }
  }
}
