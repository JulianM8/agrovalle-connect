package com.agrovalle.connect.repository;

import com.agrovalle.connect.model.Pago;
import com.agrovalle.connect.model.enums.EstadoPago;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para la entidad Pago.
 */
public interface PagoRepository extends JpaRepository<Pago, Long> {

  /**
   * Obtiene el intento de pago más reciente de un pedido.
   */
  Optional<Pago> findFirstByPedidoIdOrderByIdDesc(Long pedidoId);

  /**
   * Obtiene los pagos de un pedido que están en el estado indicado.
   */
  List<Pago> findByPedidoIdAndEstado(Long pedidoId, EstadoPago estado);
}