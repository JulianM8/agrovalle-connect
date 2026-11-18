package com.agrovalle.connect.repository;

import com.agrovalle.connect.model.Ruta;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para la entidad Ruta.
 */
public interface RutaRepository extends JpaRepository<Ruta, Long> {

  Optional<Ruta> findByVehiculoIdAndFecha(Long vehiculoId, LocalDate fecha);
}
