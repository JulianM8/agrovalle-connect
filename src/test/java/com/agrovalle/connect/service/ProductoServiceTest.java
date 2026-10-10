package com.agrovalle.connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agrovalle.connect.dto.ProductoPublicacionRequest;
import com.agrovalle.connect.dto.ProductoPublicacionResponse;
import com.agrovalle.connect.exception.AgricultorNoEncontradoException;
import com.agrovalle.connect.exception.FechaCosechaInvalidaException;
import com.agrovalle.connect.model.Agricultor;
import com.agrovalle.connect.model.Producto;
import com.agrovalle.connect.repository.AgricultorRepository;
import com.agrovalle.connect.repository.ProductoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

  @Mock
  private ProductoRepository productoRepository;

  @Mock
  private AgricultorRepository agricultorRepository;

  @InjectMocks
  private ProductoService productoService;

  private Agricultor crearAgricultor() {
    Agricultor agricultor = new Agricultor(
        "Juan", "Perez", "1111078900", "Palmira", "3001234567",
        "juan@correo.com", "hash");
    agricultor.setId(1L);
    return agricultor;
  }

  private ProductoPublicacionRequest crearRequestValido() {
    return new ProductoPublicacionRequest(
        "Aguacate", "Frutas", 50, new BigDecimal("3500"), LocalDate.now().plusDays(5));
  }

  @Test
  void debePublicarProductoConFechaValida() {
    Agricultor agricultor = crearAgricultor();
    when(agricultorRepository.findById(1L)).thenReturn(Optional.of(agricultor));
    when(productoRepository.save(any(Producto.class))).thenAnswer(invocacion -> {
      Producto producto = invocacion.getArgument(0);
      producto.setId(10L);
      return producto;
    });

    ProductoPublicacionResponse respuesta = productoService.publicar(crearRequestValido(), 1L);

    assertEquals(10L, respuesta.id());
    assertEquals(1L, respuesta.agricultorId());
    verify(productoRepository).save(any(Producto.class));
  }

  @Test
  void debeRechazarFechaDeCosechaPasada() {
    Agricultor agricultor = crearAgricultor();
    when(agricultorRepository.findById(1L)).thenReturn(Optional.of(agricultor));

    ProductoPublicacionRequest requestFechaPasada = new ProductoPublicacionRequest(
        "Aguacate", "Frutas", 50, new BigDecimal("3500"), LocalDate.now().minusDays(1));

    assertThrows(FechaCosechaInvalidaException.class,
        () -> productoService.publicar(requestFechaPasada, 1L));

    verify(productoRepository, never()).save(any(Producto.class));
  }

  @Test
  void debeRechazarAgricultorInexistente() {
    when(agricultorRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(AgricultorNoEncontradoException.class,
        () -> productoService.publicar(crearRequestValido(), 99L));

    verify(productoRepository, never()).save(any(Producto.class));
  }
}
