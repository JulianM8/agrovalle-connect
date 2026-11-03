package com.agrovalle.connect.repository;

import com.agrovalle.connect.model.Pedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para la entidad Pedido.
 */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

  /**
   * Lista los pedidos de un agricultor, del más reciente al más antiguo.
   */
  List<Pedido> findByAgricultorIdOrderByFechaCreacionDesc(Long agricultorId);
}