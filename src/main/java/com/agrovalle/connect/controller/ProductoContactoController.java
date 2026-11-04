package com.agrovalle.connect.controller;

import com.agrovalle.connect.dto.ContactoRequest;
import com.agrovalle.connect.dto.ContactoResponse;
import com.agrovalle.connect.service.ContactoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone el envío de mensajes de contacto sobre un producto.
 */
@RestController
@RequestMapping("/api/v1/productos")
public class ProductoContactoController {

  private final ContactoService contactoService;

  public ProductoContactoController(ContactoService contactoService) {
    this.contactoService = contactoService;
  }

  /**
   * Registra un contacto del comprador autenticado y responde 201.
   */
  @PostMapping("/{id}/contacto")
  @PreAuthorize("hasRole('COMPRADOR')")
  public ResponseEntity<ContactoResponse> enviar(@PathVariable("id") Long id,
      @Valid @RequestBody ContactoRequest request, Authentication authentication) {
    Long usuarioId = (Long) authentication.getPrincipal();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(contactoService.enviar(id, request, usuarioId));
  }
}
