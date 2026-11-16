package com.agrovalle.connect.repository;

import com.agrovalle.connect.model.Transportista;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para la entidad Transportista.
 */
public interface TransportistaRepository extends JpaRepository<Transportista, Long> {

  Optional<Transportista> findByUsuarioId(Long usuarioId);
}