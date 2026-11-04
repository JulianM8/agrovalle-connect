package com.agrovalle.connect.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.agrovalle.connect.model.Agricultor;
import com.agrovalle.connect.model.Comprador;
import com.agrovalle.connect.model.Contacto;
import com.agrovalle.connect.model.Producto;
import com.agrovalle.connect.model.Usuario;
import com.agrovalle.connect.model.enums.Rol;
import com.agrovalle.connect.model.enums.UnidadMedida;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ContactoPersistenceTest {

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void guardaContactoConLasClavesYColumnasDelDer() {
    Usuario usuarioAgricultor = new Usuario("1111111111", "Ana", "Campo", "3001111111",
        "ana@example.com", "clave", Rol.AGRICULTOR);
    Usuario usuarioComprador = new Usuario("2222222222", "Luis", "Mercado", "3002222222",
        "luis@example.com", "clave", Rol.COMPRADOR);
    entityManager.persist(usuarioAgricultor);
    entityManager.persist(usuarioComprador);

    Agricultor agricultor = new Agricultor("Palmira", usuarioAgricultor);
    Comprador comprador = new Comprador("Cali", "Calle 1", null, null, usuarioComprador);
    entityManager.persist(agricultor);
    entityManager.persist(comprador);

    Producto producto = new Producto("Tomate", "Verduras", 10, UnidadMedida.KILOGRAMO,
        new BigDecimal("3500"), LocalDate.now().plusDays(5), agricultor);
    entityManager.persist(producto);

    Contacto contacto = new Contacto(comprador, producto, "M".repeat(1000));
    entityManager.persist(contacto);
    entityManager.flush();

    Map<String, Object> fila = jdbcTemplate.queryForMap(
        "SELECT comprador_id, producto_id, mensaje, fecha_envio, respuesta, fecha_respuesta "
            + "FROM contactos WHERE id = ?", contacto.getId());

    assertNotNull(contacto.getId());
    assertEquals(comprador.getId(), ((Number) fila.get("COMPRADOR_ID")).longValue());
    assertEquals(producto.getId(), ((Number) fila.get("PRODUCTO_ID")).longValue());
    assertEquals("M".repeat(1000), fila.get("MENSAJE"));
    assertNotNull(fila.get("FECHA_ENVIO"));
    assertNull(fila.get("RESPUESTA"));
    assertNull(fila.get("FECHA_RESPUESTA"));
    assertEquals(agricultor.getId(), contacto.getProducto().getAgricultor().getId());
  }
}
