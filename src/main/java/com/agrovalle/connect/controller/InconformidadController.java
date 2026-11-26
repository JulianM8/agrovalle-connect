package com.agrovalle.connect.controller;

import com.agrovalle.connect.dto.InconformidadRequest;
import com.agrovalle.connect.dto.InconformidadResponse;
import com.agrovalle.connect.service.InconformidadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Permite al comprador registrar y consultar inconformidades de su pedido.
 */
@RestController
@RequestMapping("/api/v1/pedidos/{id}/inconformidad")
public class InconformidadController {

  private final InconformidadService inconformidadService;

  public InconformidadController(InconformidadService inconformidadService) {
    this.inconformidadService = inconformidadService;
  }

  /**
   * Registra una inconformidad para el pedido del comprador autenticado.
   */
  @PostMapping
  @PreAuthorize("hasRole('COMPRADOR')")
  public ResponseEntity<InconformidadResponse> reportar(@PathVariable("id") Long id,
      @Valid @RequestBody InconformidadRequest request, Authentication authentication) {
    Long usuarioId = (Long) authentication.getPrincipal();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(inconformidadService.reportar(id, request, usuarioId));
  }

  /**
   * Consulta la revisión del reporte más reciente del pedido.
   */
  @GetMapping
  @PreAuthorize("hasRole('COMPRADOR')")
  public ResponseEntity<InconformidadResponse> consultar(@PathVariable("id") Long id,
      Authentication authentication) {
    Long usuarioId = (Long) authentication.getPrincipal();
    return ResponseEntity.ok(inconformidadService.consultar(id, usuarioId));
  }
}
