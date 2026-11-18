package com.agrovalle.connect.dto;

/**
 * Datos que el sistema devuelve tras registrar a un transportista.
 */
public record TransportistaRegistroResponse(
    Long id,
    String nombre,
    String apellido,
    String identificacion,
    String municipio,
    String mensaje) {
}