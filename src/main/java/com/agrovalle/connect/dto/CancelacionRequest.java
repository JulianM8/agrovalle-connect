package com.agrovalle.connect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitud para cancelar un pedido indicando el motivo.
 */
public record CancelacionRequest(@NotBlank @Size(max = 500) String motivo) {
}