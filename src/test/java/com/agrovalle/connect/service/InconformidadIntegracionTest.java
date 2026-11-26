package com.agrovalle.connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.agrovalle.connect.dto.InconformidadRequest;
import com.agrovalle.connect.dto.InconformidadResponse;
import com.agrovalle.connect.model.Agricultor;
import com.agrovalle.connect.model.Comprador;
import com.agrovalle.connect.model.Pago;
import com.agrovalle.connect.model.Pedido;
import com.agrovalle.connect.model.Usuario;
import com.agrovalle.connect.model.enums.EstadoInconformidad;
import com.agrovalle.connect.model.enums.EstadoPago;
import com.agrovalle.connect.model.enums.EstadoPedido;
import com.agrovalle.connect.model.enums.Rol;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=never"
})
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "HU15_TEST_JDBC_URL", matches = "jdbc:postgresql://.+")
@Transactional
class InconformidadIntegracionTest {

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private InconformidadService inconformidadService;

  @DynamicPropertySource
  static void configurarPostgres(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", () -> System.getenv("HU15_TEST_JDBC_URL"));
    registry.add("spring.datasource.username", () -> System.getenv("HU15_TEST_DB_USER"));
    registry.add("spring.datasource.password", () -> System.getenv("HU15_TEST_DB_PASSWORD"));
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
  }

  @Test
  void debePersistirYConsultarElReporteSinModificarElPago() {
    Usuario usuarioComprador = crearUsuario(Rol.COMPRADOR);
    Comprador comprador = new Comprador("Palmira", "Dirección de prueba", null, null, usuarioComprador);
    entityManager.persist(comprador);
    Agricultor agricultor = new Agricultor("Palmira", crearUsuario(Rol.AGRICULTOR));
    entityManager.persist(agricultor);

    Pedido pedido = new Pedido(comprador, agricultor, "Dirección de prueba");
    pedido.setEstado(EstadoPedido.EN_TRANSPORTE);
    entityManager.persist(pedido);
    Pago pago = new Pago(pedido, "PSE", UUID.randomUUID().toString(), new BigDecimal("3500.00"));
    pago.setEstado(EstadoPago.COMPLETADO);
    entityManager.persist(pago);
    entityManager.flush();
    entityManager.clear();

    InconformidadRequest reporte =
        new InconformidadRequest("Producto dañado", "https://example.com/evidencia.jpg");
    InconformidadResponse creada =
        inconformidadService.reportar(pedido.getId(), reporte, usuarioComprador.getId());
    entityManager.flush();
    entityManager.clear();

    InconformidadResponse consultada =
        inconformidadService.consultar(pedido.getId(), usuarioComprador.getId());
    assertNotNull(creada.id());
    assertEquals(creada.id(), consultada.id());
    assertEquals(reporte.descripcion(), consultada.descripcion());
    assertEquals(reporte.evidenciaUrl(), consultada.evidenciaUrl());
    assertEquals(EstadoInconformidad.EN_REVISION, consultada.estado());
    assertNotNull(consultada.fechaReporte());

    Pago pagoPersistido = entityManager.find(Pago.class, pago.getId());
    assertEquals(EstadoPago.COMPLETADO, pagoPersistido.getEstado());
    assertEquals(pago.getReferenciaTransaccion(), pagoPersistido.getReferenciaTransaccion());
    assertEquals(pago.getMonto(), pagoPersistido.getMonto());
    assertEquals(pago.getFechaLiberacion(), pagoPersistido.getFechaLiberacion());
  }

  private Usuario crearUsuario(Rol rol) {
    String identificacion = UUID.randomUUID().toString().substring(0, 12);
    Usuario usuario = new Usuario(identificacion, "Prueba", "HU15", "3001234567",
        identificacion + "@agrovalle.test", "clave-de-prueba", rol);
    entityManager.persist(usuario);
    return usuario;
  }
}
