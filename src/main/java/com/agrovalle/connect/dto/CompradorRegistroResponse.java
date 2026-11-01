package com.agrovalle.connect.dto;

/**
 * Datos que el sistema devuelve tras registrar a un comprador.
 */
public record CompradorRegistroResponse(
    Long id,
    String nombre,
    String apellido,
    String identificacion,
    String municipio,
    String direccionEntrega,
    String mensaje) {
}
