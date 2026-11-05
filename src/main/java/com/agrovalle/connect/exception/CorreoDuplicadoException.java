package com.agrovalle.connect.exception;

/**
 * Se lanza cuando se intenta registrar un usuario con un correo ya existente.
 */
public class CorreoDuplicadoException extends RuntimeException {
  public CorreoDuplicadoException(String correo) {
    super("Ya existe un usuario registrado con el correo: " + correo);
  }
}
