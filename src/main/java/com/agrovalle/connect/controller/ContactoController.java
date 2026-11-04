package com.agrovalle.connect.controller;

import com.agrovalle.connect.dto.ContactoRespuestaRequest;
import com.agrovalle.connect.dto.ContactoResponse;
import com.agrovalle.connect.service.ContactoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone las respuestas a solicitudes de contacto.
 */
@RestController
@RequestMapping("/api/v1/contactos")
public class ContactoController {

  private final ContactoService contactoService;

  public ContactoController(ContactoService contactoService) {
    this.contactoService = contactoService;
  }

  /**
   * Permite al agricultor dueño del producto responder un contacto.
   */
  @PostMapping("/{id}/respuesta")
  @PreAuthorize("hasRole('AGRICULTOR')")
  public ResponseEntity<ContactoResponse> responder(@PathVariable("id") Long id,
      @Valid @RequestBody ContactoRespuestaRequest request, Authentication authentication) {
    Long usuarioId = (Long) authentication.getPrincipal();
    return ResponseEntity.ok(contactoService.responder(id, request.respuesta(), usuarioId));
  }
}
