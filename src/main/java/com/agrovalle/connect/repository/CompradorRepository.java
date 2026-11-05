package com.agrovalle.connect.repository;

import com.agrovalle.connect.model.Comprador;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para la entidad Comprador.
 */
public interface CompradorRepository extends JpaRepository<Comprador, Long> {

  Optional<Comprador> findByUsuarioId(Long usuarioId);
}
