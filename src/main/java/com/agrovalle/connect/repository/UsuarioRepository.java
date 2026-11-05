package com.agrovalle.connect.repository;

import com.agrovalle.connect.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para la entidad Usuario.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

  boolean existsByIdentificacion(String identificacion);

  boolean existsByCorreo(String correo);

  Optional<Usuario> findByIdentificacion(String identificacion);
}
