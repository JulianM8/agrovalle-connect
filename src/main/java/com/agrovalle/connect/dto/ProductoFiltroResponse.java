package com.agrovalle.connect.dto;

import java.util.List;

/**
 * Respuesta de la consulta filtrada del catálogo de productos.
 */
public record ProductoFiltroResponse(
    List<ProductoConsultaResponse> productos,
    String mensaje) {
}
