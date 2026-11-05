package com.agrovalle.connect.exception;

/**
 * Se lanza cuando un recurso solicitado no existe (responde 404).
 */
public class RecursoNoEncontradoException extends RuntimeException {

  public RecursoNoEncontradoException(String mensaje) {
    super(mensaje);
  }
}