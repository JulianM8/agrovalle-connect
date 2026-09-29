package com.agrovalle.connect.exception;
 
import java.time.LocalDate;
 
/**
* Se lanza cuando la fecha de cosecha de un producto es anterior a hoy.
*/
public class FechaCosechaInvalidaException extends RuntimeException {
 
  public FechaCosechaInvalidaException(LocalDate fechaCosecha) {
    super("La fecha de cosecha no puede ser anterior a hoy: " + fechaCosecha);
  }
}