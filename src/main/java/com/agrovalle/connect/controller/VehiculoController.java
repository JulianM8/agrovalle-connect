package com.agrovalle.connect.controller;

import com.agrovalle.connect.dto.VehiculoRequest;
import com.agrovalle.connect.dto.VehiculosPorTipoResponse;
import com.agrovalle.connect.dto.VehiculoResponse;
import com.agrovalle.connect.service.VehiculoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone el registro y la consulta de vehículos de transporte.
 */
@RestController
@RequestMapping("/api/v1/vehiculos")
public class VehiculoController {

  private final VehiculoService vehiculoService;

  public VehiculoController(VehiculoService vehiculoService) {
    this.vehiculoService = vehiculoService;
  }

  /**
   * Registra el vehículo del transportista autenticado (201, 400, 409).
   */
  @PostMapping
  @PreAuthorize("hasRole('TRANSPORTISTA')")
  public ResponseEntity<VehiculoResponse> registrar(
      @Valid @RequestBody VehiculoRequest request, Authentication authentication) {
    Long usuarioId = (Long) authentication.getPrincipal();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(vehiculoService.registrar(request, usuarioId));
  }

    /**
   * Lista los vehículos de un tipo de transporte para que el agricultor elija.
   */
  @GetMapping
  @PreAuthorize("hasRole('AGRICULTOR')")
  public ResponseEntity<VehiculosPorTipoResponse> listarPorTipo(
      @RequestParam(name = "tipoTransporte", required = false) String tipoTransporte) {
    return ResponseEntity.ok(vehiculoService.listarPorTipo(tipoTransporte));
  }
}
