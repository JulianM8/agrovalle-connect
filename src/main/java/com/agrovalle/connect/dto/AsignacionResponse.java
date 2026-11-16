package com.agrovalle.connect.dto;

import com.agrovalle.connect.model.enums.EstadoPedido;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Resultado de asignar un vehículo a un pedido.
 */
public record AsignacionResponse(
    Long id,
    Long pedidoId,
    EstadoPedido estadoPedido,
    Long vehiculoId,
    Long rutaId,
    LocalDate fechaRuta,
    Integer ordenEntrega,
    LocalDateTime fechaAsignacion) {
}
