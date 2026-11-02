package com.agrovalle.connect.dto;

import com.agrovalle.connect.model.enums.EstadoPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta con el pedido creado y su total calculado.
 */
public record PedidoResponse(
    Long id,
    EstadoPedido estado,
    String direccionEntrega,
    LocalDateTime fechaCreacion,
    Long agricultorId,
    List<DetallePedidoResponse> detalles,
    BigDecimal total) {
}