package com.agrovalle.connect.repository;

import com.agrovalle.connect.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para la entidad Pedido.
 */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}