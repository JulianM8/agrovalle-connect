package com.agrovalle.connect.repository;

import com.agrovalle.connect.model.Pedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio de acceso a datos para la entidad Pedido.
 */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

  /**
   * Lista los pedidos de un agricultor, del más reciente al más antiguo.
   */
  List<Pedido> findByAgricultorIdOrderByFechaCreacionDesc(Long agricultorId);

  /**
   * Busca el pedido bloqueando su fila hasta que termine la transacción,
   * para que dos pagos simultáneos del mismo pedido no se pisen.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Pedido p where p.id = :id")
  Optional<Pedido> findByIdParaActualizar(@Param("id") Long id);
}