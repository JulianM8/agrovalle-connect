package com.agrovalle.connect.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.agrovalle.connect.model.Agricultor;
import com.agrovalle.connect.model.Comprador;
import com.agrovalle.connect.model.Contacto;
import com.agrovalle.connect.model.Producto;
import com.agrovalle.connect.model.Usuario;
import com.agrovalle.connect.model.enums.Rol;
import com.agrovalle.connect.model.enums.UnidadMedida;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ContactoRepositoryTest {

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private AgricultorRepository agricultorRepository;

  @Autowired
  private CompradorRepository compradorRepository;

  @Autowired
  private ProductoRepository productoRepository;

  @Autowired
  private ContactoRepository contactoRepository;

  @Test
  void debeFiltrarPorAgricultorYPendientes() {
    Agricultor agricultor = crearAgricultor("1111000001");
    Agricultor otroAgricultor = crearAgricultor("1111000002");
    Usuario usuarioComprador = usuarioRepository.save(new Usuario("1111000003", "Comprador",
        "Prueba", "3001234567", "comprador-contactos@correo.com", "hash", Rol.COMPRADOR));
    Comprador comprador = compradorRepository.save(new Comprador(
        "Cali", "Calle 1", null, null, usuarioComprador));
    Producto producto = crearProducto(agricultor);
    Producto productoAjeno = crearProducto(otroAgricultor);

    Contacto pendiente = contactoRepository.save(new Contacto(comprador, producto, "Pendiente"));
    Contacto respondido = new Contacto(comprador, producto, "Respondido");
    respondido.setRespuesta("Disponible");
    respondido.setFechaRespuesta(LocalDateTime.now());
    contactoRepository.save(respondido);
    contactoRepository.save(new Contacto(comprador, productoAjeno, "De otro agricultor"));

    Set<Long> todos = contactoRepository.listarDelAgricultor(agricultor.getUsuario().getId(),
        false).stream().map(Contacto::getId).collect(java.util.stream.Collectors.toSet());
    Set<Long> soloPendientes = contactoRepository.listarDelAgricultor(
        agricultor.getUsuario().getId(), true).stream().map(Contacto::getId)
        .collect(java.util.stream.Collectors.toSet());

    assertEquals(Set.of(pendiente.getId(), respondido.getId()), todos);
    assertEquals(Set.of(pendiente.getId()), soloPendientes);
  }

  private Agricultor crearAgricultor(String identificacion) {
    Usuario usuario = usuarioRepository.save(new Usuario(identificacion, "Agricultor",
        "Prueba", "3001234567", identificacion + "@correo.com", "hash", Rol.AGRICULTOR));
    return agricultorRepository.save(new Agricultor("Palmira", usuario));
  }

  private Producto crearProducto(Agricultor agricultor) {
    return productoRepository.save(new Producto("Aguacate", "Frutas", 10,
        UnidadMedida.KILOGRAMO, new BigDecimal("3500"), LocalDate.now().plusDays(5),
        agricultor));
  }
}
