package com.agrovalle.connect.repository;

import com.agrovalle.connect.model.Inconformidad;
import com.agrovalle.connect.model.enums.EstadoInconformidad;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Consultas de inconformidades asociadas a pedidos.
 */
public interface InconformidadRepository extends JpaRepository<Inconformidad, Long> {

  List<Inconformidad> findByPedidoIdOrderByFechaReporteDesc(Long pedidoId);

  boolean existsByPedidoIdAndEstado(Long pedidoId, EstadoInconformidad estado);
}
