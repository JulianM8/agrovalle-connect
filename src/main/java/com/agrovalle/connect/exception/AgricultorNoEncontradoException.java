package com.agrovalle.connect.exception;
 
/**
* Se lanza cuando el usuario autenticado no tiene un perfil de agricultor
* asociado (por ejemplo, al intentar publicar un producto).
*/
public class AgricultorNoEncontradoException extends RuntimeException {

  public AgricultorNoEncontradoException(Long usuarioId) {
    super("No existe un perfil de agricultor para el usuario con id: " + usuarioId);
  }
}
