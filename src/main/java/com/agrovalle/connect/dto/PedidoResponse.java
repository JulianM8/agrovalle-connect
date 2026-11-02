package com.agrovalle.connect.dto;

import com.agrovalle.connect.model.enums.EstadoPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
    Long id,
    EstadoPedido estado,
    Long agricultorId,
    BigDecimal total,
    LocalDateTime fechaCreacion,
    List<DetallePedidoResponse> items) {
}