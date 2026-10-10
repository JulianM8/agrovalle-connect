package com.agrovalle.connect.exception;

public class CredencialesInvalidasException extends RuntimeException {
  public CredencialesInvalidasException() {
    super("Identificación o contraseña incorrectas");
  }
}