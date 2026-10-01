package com.agrovalle.connect.dto;

/**
 * Datos que el sistema devuelve tras registrar a un agricultor.
 */
public record AgricultorRegistroResponse(
    Long id,
    String nombre,
    String apellido,
    String identificacion,
    String municipio,
    String mensaje) {
}
