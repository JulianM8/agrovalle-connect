package com.agrovalle.connect.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Solicitud para crear un pedido a partir de un carrito de productos.
 */
public record PedidoRequest(
    @NotEmpty @Valid List<DetallePedidoRequest> items,
    @NotBlank String direccionEntrega) {
}