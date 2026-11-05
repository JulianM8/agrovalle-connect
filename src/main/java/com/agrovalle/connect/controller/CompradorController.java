package com.agrovalle.connect.controller;

import com.agrovalle.connect.dto.CompradorRegistroRequest;
import com.agrovalle.connect.dto.CompradorRegistroResponse;
import com.agrovalle.connect.service.CompradorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone el registro de compradores.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class CompradorController {

  private final CompradorService compradorService;

  public CompradorController(CompradorService compradorService) {
    this.compradorService = compradorService;
  }

  /**
   * Registra un nuevo comprador, o agrega el rol COMPRADOR a una cuenta existente.
   */
  @PostMapping("/register/comprador")
  public ResponseEntity<CompradorRegistroResponse> registrar(
      @Valid @RequestBody CompradorRegistroRequest request) {
    CompradorRegistroResponse respuesta = compradorService.registrar(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
  }
}
