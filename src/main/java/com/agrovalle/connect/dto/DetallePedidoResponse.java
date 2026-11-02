package com.agrovalle.connect.dto;

import java.math.BigDecimal;

public record DetallePedidoResponse(
    Long productoId,
    String nombre,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal) {
}