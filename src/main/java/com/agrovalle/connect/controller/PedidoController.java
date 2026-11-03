package com.agrovalle.connect.controller;

import com.agrovalle.connect.dto.PagoRequest;
import com.agrovalle.connect.dto.PagoResponse;
import com.agrovalle.connect.dto.PedidoRequest;
import com.agrovalle.connect.dto.PedidoResponse;
import com.agrovalle.connect.service.PagoService;
import com.agrovalle.connect.service.PedidoService;
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
 * Expone la creación de pedidos y el inicio y consulta de su pago.
 */
@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

  private final PedidoService pedidoService;

  private final PagoService pagoService;

  public PedidoController(PedidoService pedidoService, PagoService pagoService) {
    this.pedidoService = pedidoService;
    this.pagoService = pagoService;
  }

  /**
   * Crea un pedido para el comprador autenticado.
   */
  @PostMapping
  @PreAuthorize("hasRole('COMPRADOR')")
  public ResponseEntity<PedidoResponse> crear(
      @Valid @RequestBody PedidoRequest request, Authentication authentication) {
    Long usuarioId = (Long) authentication.getPrincipal();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(pedidoService.crear(request, usuarioId));
  }

  /**
   * Inicia el pago de un pedido del comprador autenticado.
   */
  @PostMapping("/{id}/pago")
  @PreAuthorize("hasRole('COMPRADOR')")
  public ResponseEntity<PagoResponse> pagar(@PathVariable("id") Long id,
      @Valid @RequestBody PagoRequest request, Authentication authentication) {
    Long usuarioId = (Long) authentication.getPrincipal();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(pagoService.pagar(id, request, usuarioId));
  }

  /**
   * Consulta el estado del pago más reciente de un pedido.
   */
  @GetMapping("/{id}/pago")
  @PreAuthorize("hasRole('COMPRADOR')")
  public ResponseEntity<PagoResponse> consultarPago(@PathVariable("id") Long id,
      Authentication authentication) {
    Long usuarioId = (Long) authentication.getPrincipal();
    return ResponseEntity.ok(pagoService.consultar(id, usuarioId));
  }
}