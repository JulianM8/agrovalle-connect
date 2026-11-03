package com.agrovalle.connect.dto;

import com.agrovalle.connect.model.enums.EstadoPedido;
import java.time.LocalDateTime;

/**
 * Fila del listado de pedidos del agricultor, con el motivo si fue cancelado.
 */
public record PedidoAgricultorResponse(
    Long id,
    EstadoPedido estado,
    String direccionEntrega,
    LocalDateTime fechaCreacion,
    String motivoCancelacion) {
}