package com.agrovalle.connect.exception;

/**
 * Se lanza cuando la solicitud choca con el estado actual de un recurso,
 * por ejemplo una placa duplicada o un pedido ya asignado (responde 409).
 */
public class ConflictoException extends RuntimeException {

  public ConflictoException(String mensaje) {
    super(mensaje);
  }
}
