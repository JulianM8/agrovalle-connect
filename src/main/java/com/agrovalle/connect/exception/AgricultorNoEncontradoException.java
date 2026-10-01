package com.agrovalle.connect.exception;
 
/**
* Se lanza cuando se publica un producto a nombre de un agricultor que no existe.
*/
public class AgricultorNoEncontradoException extends RuntimeException {
 
  public AgricultorNoEncontradoException(Long agricultorId) {
    super("No existe un agricultor con id: " + agricultorId);
  }
}