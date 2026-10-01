package com.agrovalle.connect.repository;

import com.agrovalle.connect.model.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para la entidad Producto.
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca productos por el municipio del agricultor y su categoría.
     *
     * @param municipio municipio de origen del agricultor
     * @param categoria categoría del producto
     * @return productos que coinciden con ambos criterios
     */
    List<Producto> findByAgricultorMunicipioAndCategoria(String municipio, String categoria);
}
