package com.agrovalle.connect.exception;

/**
 * Se lanza cuando se intenta registrar un agricultor con una identificación ya existente.
 */
public class IdentificacionDuplicadaException extends RuntimeException {
  public IdentificacionDuplicadaException(String identificacion) {
    super("Ya existe un agricultor registrado con la identificación: " + identificacion);
  }
}
