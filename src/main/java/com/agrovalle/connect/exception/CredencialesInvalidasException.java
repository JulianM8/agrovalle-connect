package com.agrovalle.connect.exception;

/**
 * Excepción lanzada cuando las credenciales de inicio de sesión son inválidas.
 */
public class CredencialesInvalidasException extends RuntimeException {
  public CredencialesInvalidasException() {
    super("Identificación o contraseña incorrectas");
  }
}