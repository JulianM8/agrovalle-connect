package com.agrovalle.connect.dto;

import java.time.LocalDateTime;

/**
 * Datos de una solicitud de contacto y su respuesta, si ya existe.
 */
public record ContactoResponse(
    Long id,
    Long compradorId,
    Long productoId,
    String mensaje,
    LocalDateTime fechaEnvio,
    String respuesta,
    LocalDateTime fechaRespuesta) {
}
