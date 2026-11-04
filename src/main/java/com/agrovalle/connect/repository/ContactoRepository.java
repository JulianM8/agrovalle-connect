package com.agrovalle.connect.repository;

import com.agrovalle.connect.model.Contacto;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para la entidad Contacto.
 */
public interface ContactoRepository extends JpaRepository<Contacto, Long> {
}
