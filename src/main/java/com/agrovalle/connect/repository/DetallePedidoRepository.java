package com.agrovalle.connect.repository;

import com.agrovalle.connect.model.DetallePedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para la entidad DetallePedido.
 */
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

  /**
   * Obtiene las líneas de un pedido, necesarias para calcular su total.
   */
  List<DetallePedido> findByPedidoId(Long pedidoId);
}