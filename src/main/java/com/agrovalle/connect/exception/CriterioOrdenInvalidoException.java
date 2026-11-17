package com.agrovalle.connect.exception;

/**
 * Se lanza cuando el criterio de ordenamiento de entregas solicitado no existe.
 */
public class CriterioOrdenInvalidoException extends RuntimeException {

  public CriterioOrdenInvalidoException(String criterio) {
    super("Criterio de ordenamiento no soportado: " + criterio);
  }
}