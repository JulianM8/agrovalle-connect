package com.agrovalle.connect.dto;

import com.agrovalle.connect.model.enums.EstadoPedido;
import java.time.LocalDateTime;

/**
 * Respuesta de la confirmación de recepción de un pedido.
 */
public record RecepcionResponse(
    Long pedidoId,
    EstadoPedido estado,
    LocalDateTime fechaEntrega) {
}