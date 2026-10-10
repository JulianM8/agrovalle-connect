package com.agrovalle.connect.exception;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Traduce las excepciones de negocio y de validación en respuestas HTTP.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IdentificacionDuplicadaException.class)
  public ResponseEntity<Map<String, String>> manejarIdentificacionDuplicada(
      IdentificacionDuplicadaException ex) {
    return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(AgricultorNoEncontradoException.class)
  public ResponseEntity<Map<String, String>> manejarAgricultorNoEncontrado(
      AgricultorNoEncontradoException ex) {
    return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
  }

 @ExceptionHandler(MissingServletRequestParameterException.class)
public ResponseEntity<Map<String, String>> manejarParametroFaltante(
    MissingServletRequestParameterException ex, HttpServletRequest request) {
  Map<String, String> errores = new LinkedHashMap<>();

  if (request.getParameter("categoria") == null) {
    errores.put("categoria", "La categoría es obligatoria.");
  }
  if (request.getParameter("municipio") == null) {
    errores.put("municipio", "El municipio es obligatorio.");
  }
  return ResponseEntity.badRequest().body(errores);
}

  @ExceptionHandler(FechaCosechaInvalidaException.class)
  public ResponseEntity<Map<String, String>> manejarFechaCosechaInvalida(
      FechaCosechaInvalidaException ex) {
    return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> manejarDatosInvalidos(
      MethodArgumentNotValidException ex) {
    Map<String, String> errores = new HashMap<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
    return ResponseEntity.badRequest().body(errores);
  }

  @ExceptionHandler(CredencialesInvalidasException.class)
  public ResponseEntity<Map<String, String>> manejarCredencialesInvalidas(
      CredencialesInvalidasException ex) {
    return construirRespuesta(HttpStatus.UNAUTHORIZED, ex.getMessage());
  }

  private ResponseEntity<Map<String, String>> construirRespuesta(
      HttpStatus estado, String mensaje) {
    Map<String, String> cuerpo = new HashMap<>();
    cuerpo.put("mensaje", mensaje);
    return ResponseEntity.status(estado).body(cuerpo);
  }
}