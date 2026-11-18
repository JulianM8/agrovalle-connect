package com.agrovalle.connect.controller;

import com.agrovalle.connect.dto.TransportistaRegistroRequest;
import com.agrovalle.connect.dto.TransportistaRegistroResponse;
import com.agrovalle.connect.service.TransportistaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone el registro de transportistas.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class TransportistaController {

  private final TransportistaService transportistaService;

  public TransportistaController(TransportistaService transportistaService) {
    this.transportistaService = transportistaService;
  }

  /**
   * Registra un nuevo transportista, o agrega el rol TRANSPORTISTA a una cuenta existente.
   */
  @PostMapping("/register/transportista")
  public ResponseEntity<TransportistaRegistroResponse> registrar(
      @Valid @RequestBody TransportistaRegistroRequest request) {
    TransportistaRegistroResponse respuesta = transportistaService.registrar(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
  }
}
