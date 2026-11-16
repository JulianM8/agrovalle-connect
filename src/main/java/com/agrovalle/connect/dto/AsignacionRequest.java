package com.agrovalle.connect.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Solicitud para asignar un vehículo a un pedido.
 */
public record AsignacionRequest(
    @NotNull(message = "El vehículo es obligatorio") Long vehiculoId) {
}
