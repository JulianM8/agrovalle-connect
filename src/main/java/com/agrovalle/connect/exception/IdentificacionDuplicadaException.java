package com.agrovalle.connect.exception;

/**
 * Se lanza cuando se intenta registrar un usuario con una identificación ya existente.
 */
public class IdentificacionDuplicadaException extends RuntimeException {
  public IdentificacionDuplicadaException(String identificacion) {
    super("Ya existe un usuario registrado con la identificación: " + identificacion);
  }
}
