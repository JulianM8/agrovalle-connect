package com.agrovalle.connect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Respuesta que el agricultor envía a una solicitud de contacto.
 */
public record ContactoRespuestaRequest(
    @NotBlank @Size(max = 1000) String respuesta) {
}
