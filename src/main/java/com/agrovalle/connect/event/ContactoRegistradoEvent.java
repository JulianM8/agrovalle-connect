package com.agrovalle.connect.event;

/**
 * Identifica el contacto nuevo y al agricultor que recibirá la solicitud.
 */
public record ContactoRegistradoEvent(Long contactoId, Long agricultorId) {
}
