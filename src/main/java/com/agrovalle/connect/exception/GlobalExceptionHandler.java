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

@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Maneja la excepción IdentificacionDuplicadaException y devuelve una respuesta
   * con el estado HTTP 409 (Conflict).
   */
  @ExceptionHandler(IdentificacionDuplicadaException.class)
  public ResponseEntity<Map<String, String>> manejarIdentificacionDuplicada(
      IdentificacionDuplicadaException ex) {
    return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
  }

  /**
   * Maneja la excepción AgricultorNoEncontradoException y devuelve una respuesta
   * con el estado HTTP 404 (Not Found).
   */
  @ExceptionHandler(AgricultorNoEncontradoException.class)
  public ResponseEntity<Map<String, String>> manejarAgricultorNoEncontrado(
      AgricultorNoEncontradoException ex) {
    return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  /**
   * Maneja la excepción MissingServletRequestParameterException y devuelve una
   * respuesta
   * con el estado HTTP 400 (Bad Request) indicando los parámetros faltantes.
   */
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

  /**
   * Maneja la excepción FechaCosechaInvalidaException y devuelve una respuesta
   * con el estado HTTP 400 (Bad Request).
   */
  @ExceptionHandler(FechaCosechaInvalidaException.class)
  public ResponseEntity<Map<String, String>> manejarFechaCosechaInvalida(
      FechaCosechaInvalidaException ex) {
    return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  /**
   * Maneja la excepción MethodArgumentNotValidException y devuelve una respuesta
   * con el estado HTTP 400 (Bad Request) indicando los errores de validación.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> manejarDatosInvalidos(
      MethodArgumentNotValidException ex) {
    Map<String, String> errores = new HashMap<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
    return ResponseEntity.badRequest().body(errores);
  }

  /**
   * Maneja la excepción CredencialesInvalidasException y devuelve una respuesta
   * con el estado HTTP 401 (Unauthorized).
   */
  @ExceptionHandler(CredencialesInvalidasException.class)
  public ResponseEntity<Map<String, String>> manejarCredencialesInvalidas(
      CredencialesInvalidasException ex) {
    return construirRespuesta(HttpStatus.UNAUTHORIZED, ex.getMessage());
  }

  /**
   * Construye una respuesta HTTP con el estado y mensaje especificados.
   */
  private ResponseEntity<Map<String, String>> construirRespuesta(
      HttpStatus estado, String mensaje) {
    Map<String, String> cuerpo = new HashMap<>();
    cuerpo.put("mensaje", mensaje);
    return ResponseEntity.status(estado).body(cuerpo);
  }
}