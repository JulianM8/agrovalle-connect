package com.agrovalle.connect.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce las excepciones de negocio y de validación en respuestas HTTP.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IdentificacionDuplicadaException.class)
  public ResponseEntity<Map<String, String>> manejarIdentificacionDuplicada(
      IdentificacionDuplicadaException ex) {
    Map<String, String> cuerpo = new HashMap<>();
    cuerpo.put("mensaje", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(cuerpo);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> manejarDatosInvalidos(
      MethodArgumentNotValidException ex) {
    Map<String, String> errores = new HashMap<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
    return ResponseEntity.badRequest().body(errores);
  }
}
