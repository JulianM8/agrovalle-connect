package com.agrovalle.connect.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Solicitud para pagar un pedido con un medio de pago (TARJETA, PSE, CONTRAENTREGA).
 */
public record PagoRequest(@NotBlank String medioPago) {
}