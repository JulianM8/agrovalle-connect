package com.agrovalle.connect.repository;

import com.agrovalle.connect.model.Agricultor;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para la entidad Agricultor.
 */
public interface AgricultorRepository extends JpaRepository<Agricultor, Long> {

  boolean existsByIdentificacion(String identificacion);
}