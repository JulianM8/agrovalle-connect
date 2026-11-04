package com.agrovalle.connect.repository;

import com.agrovalle.connect.model.Contacto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio de acceso a datos para la entidad Contacto.
 */
public interface ContactoRepository extends JpaRepository<Contacto, Long> {

  @Query("""
      select c from Contacto c
      where c.producto.agricultor.usuario.id = :usuarioId
        and (:pendientes = false or c.respuesta is null)
      order by c.fechaEnvio desc
      """)
  List<Contacto> listarDelAgricultor(@Param("usuarioId") Long usuarioId,
      @Param("pendientes") boolean pendientes);
}
