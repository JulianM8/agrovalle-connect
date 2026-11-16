package com.agrovalle.connect.repository;

import com.agrovalle.connect.model.Vehiculo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para la entidad Vehiculo.
 */
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

  List<Vehiculo> findByTipoTransporte(String tipoTransporte);

  boolean existsByPlaca(String placa);

  Optional<Vehiculo> findByTransportistaId(Long transportistaId);
}
