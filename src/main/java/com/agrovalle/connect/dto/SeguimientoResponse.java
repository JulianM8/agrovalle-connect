package com.agrovalle.connect.dto;

import com.agrovalle.connect.model.enums.EstadoPedido;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Seguimiento de un pedido: estado actual y fecha estimada de llegada, que es
 * nula mientras el pedido no tenga una fecha asignada.
 */
public record SeguimientoResponse(
    Long pedidoId,
    EstadoPedido estado,
    LocalDateTime fechaCreacion,
    LocalDate fechaEstimadaLlegada,
    LocalDateTime fechaEntrega,
    String mensaje) {
}
