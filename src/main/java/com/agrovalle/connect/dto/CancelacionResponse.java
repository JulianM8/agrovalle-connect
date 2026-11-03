package com.agrovalle.connect.dto;

import com.agrovalle.connect.model.enums.EstadoPedido;

/**
 * Respuesta de la cancelación: nuevo estado, motivo guardado y si hubo reembolso.
 */
public record CancelacionResponse(
    Long pedidoId,
    EstadoPedido estado,
    String motivoCancelacion,
    boolean reembolsado,
    String mensaje) {
}