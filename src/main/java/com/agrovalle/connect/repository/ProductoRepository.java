package com.agrovalle.connect.repository;

import com.agrovalle.connect.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

/** * Repositorio de acceso a datos para la entidad Producto. */
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
