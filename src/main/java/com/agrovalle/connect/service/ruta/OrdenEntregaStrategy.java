package com.agrovalle.connect.service.ruta;

import com.agrovalle.connect.model.Pedido;
import java.util.List;

/**
 * Estrategia para ordenar las entregas de una ruta. RutaService elige cuál
 * usar según el criterio solicitado, sin conocer el algoritmo.
 */
public interface OrdenEntregaStrategy {

  /**
   * Devuelve los pedidos en el orden sugerido de entrega, sin modificar la lista recibida.
   */
  List<Pedido> ordenar(List<Pedido> pedidos);
}