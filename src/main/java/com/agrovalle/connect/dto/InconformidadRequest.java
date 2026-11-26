package com.agrovalle.connect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

/**
 * Descripción y evidencia del reporte de un comprador.
 */
public record InconformidadRequest(
    @NotBlank @Size(max = 1000) String descripcion,
    @NotBlank @Size(max = 500)
    @URL(regexp = "(?i)https?://[^\\s/?#]+(?:[/?#]\\S*)?",
        message = "La evidencia debe ser una URL HTTP o HTTPS válida")
    String evidenciaUrl) {
}
