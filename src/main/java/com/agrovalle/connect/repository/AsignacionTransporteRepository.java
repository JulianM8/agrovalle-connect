package com.agrovalle.connect.repository;

import com.agrovalle.connect.model.AsignacionTransporte;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para la entidad AsignacionTransporte.
 */
public interface AsignacionTransporteRepository
    extends JpaRepository<AsignacionTransporte, Long> {

  Optional<AsignacionTransporte> findByPedidoId(Long pedidoId);

  List<AsignacionTransporte> findByRutaIdOrderByOrdenEntrega(Long rutaId);
}
