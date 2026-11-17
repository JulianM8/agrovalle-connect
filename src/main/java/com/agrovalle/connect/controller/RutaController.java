package com.agrovalle.connect.controller;

import com.agrovalle.connect.dto.RutaDiaResponse;
import com.agrovalle.connect.service.RutaService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone la consulta de la ruta del día del transportista autenticado.
 */
@RestController
@RequestMapping("/api/v1/rutas")
public class RutaController {

  private final RutaService rutaService;

  public RutaController(RutaService rutaService) {
    this.rutaService = rutaService;
  }

  /**
   * Consulta la ruta de la fecha indicada (hoy por defecto) con el orden de
   * entregas sugerido según el criterio (200, 400, 404).
   */
  @GetMapping
  @PreAuthorize("hasRole('TRANSPORTISTA')")
  public ResponseEntity<RutaDiaResponse> consultar(
      @RequestParam(name = "fecha", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
      @RequestParam(name = "criterio", required = false) String criterio,
      Authentication authentication) {
    Long usuarioId = (Long) authentication.getPrincipal();
    LocalDate dia = fecha != null ? fecha : LocalDate.now();
    return ResponseEntity.ok(rutaService.consultarRutaDelDia(usuarioId, dia, criterio));
  }
}