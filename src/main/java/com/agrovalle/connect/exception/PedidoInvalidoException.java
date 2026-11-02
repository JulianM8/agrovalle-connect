package com.agrovalle.connect.exception;

/**
 * Se lanza cuando la solicitud rompe una regla de negocio (responde 400).
 */
public class PedidoInvalidoException extends RuntimeException {

  public PedidoInvalidoException(String mensaje) {
    super(mensaje);
  }
}