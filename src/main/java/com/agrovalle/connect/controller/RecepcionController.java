package com.agrovalle.connect.controller;

import com.agrovalle.connect.dto.RecepcionResponse;
import com.agrovalle.connect.service.RecepcionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone la confirmación de recepción de un pedido para el comprador.
 */
@RestController
@RequestMapping("/api/v1/pedidos")
public class RecepcionController {

  private final RecepcionService recepcionService;

  public RecepcionController(RecepcionService recepcionService) {
    this.recepcionService = recepcionService;
  }

  /**
   * Confirma la recepción del pedido: 200, 400, 404 o 409.
   */
  @PostMapping("/{id}/recepcion")
  @PreAuthorize("hasRole('COMPRADOR')")
  public ResponseEntity<RecepcionResponse> confirmarRecepcion(
      @PathVariable("id") Long id, Authentication authentication) {
    Long usuarioId = (Long) authentication.getPrincipal();
    return ResponseEntity.ok(recepcionService.confirmarRecepcion(id, usuarioId));
  }
}