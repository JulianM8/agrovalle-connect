package com.agrovalle.connect.controller;
 
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
import com.agrovalle.connect.config.SecurityConfig;
import com.agrovalle.connect.dto.ProductoPublicacionRequest;
import com.agrovalle.connect.dto.ProductoPublicacionResponse;
import com.agrovalle.connect.exception.AgricultorNoEncontradoException;
import com.agrovalle.connect.exception.FechaCosechaInvalidaException;
import com.agrovalle.connect.exception.GlobalExceptionHandler;
import com.agrovalle.connect.model.Agricultor;
import com.agrovalle.connect.model.Producto;
import com.agrovalle.connect.service.ProductoService;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
 
@WebMvcTest(ProductoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ProductoControllerTest {
 
  @Autowired
  private MockMvc mockMvc;
 
  @Autowired
  private ObjectMapper objectMapper;
 
  @MockitoBean
  private ProductoService productoService;
 
  private ProductoPublicacionRequest crearRequestValido() {
    return new ProductoPublicacionRequest(
        1L, "Aguacate", "Frutas", 50, new BigDecimal("3500"), LocalDate.now().plusDays(5));
  }

  private Producto crearProductoConsulta() {
    Agricultor agricultor = new Agricultor(
        "Juan", "Perez", "1111078900", "Palmira", "3001234567", "juan@correo.com");
    agricultor.setId(1L);
    Producto producto = new Producto(
        "Aguacate",
        "Frutas",
        50,
        new BigDecimal("3500"),
        LocalDate.now().plusDays(5),
        agricultor);
    producto.setId(10L);
    return producto;
  }
 
  @Test
  void debePublicarProductoConFechaValida() throws Exception {
    ProductoPublicacionResponse respuesta = new ProductoPublicacionResponse(
        10L, "Aguacate", "Frutas", 50, new BigDecimal("3500"),
        LocalDate.now().plusDays(5), 1L, "Producto publicado correctamente");
    when(productoService.publicar(any(ProductoPublicacionRequest.class)))
        .thenReturn(respuesta);
 
    mockMvc.perform(post("/api/v1/productos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(crearRequestValido())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.mensaje").value("Producto publicado correctamente"));
  }
 
  @Test
  void debeRechazarFechaDeCosechaPasada() throws Exception {
    when(productoService.publicar(any(ProductoPublicacionRequest.class)))
        .thenThrow(new FechaCosechaInvalidaException(LocalDate.now().minusDays(1)));
 
    mockMvc.perform(post("/api/v1/productos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(crearRequestValido())))
        .andExpect(status().isBadRequest());
  }
 
  @Test
  void debeRechazarAgricultorInexistente() throws Exception {
    when(productoService.publicar(any(ProductoPublicacionRequest.class)))
        .thenThrow(new AgricultorNoEncontradoException(1L));
 
    mockMvc.perform(post("/api/v1/productos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(crearRequestValido())))
        .andExpect(status().isNotFound());
  }

  @Test
  void debeListarProductosFiltradosPorCategoriaYMunicipio() throws Exception {
    when(productoService.buscarDisponiblesPorMunicipioYCategoria("Palmira", "Frutas"))
        .thenReturn(List.of(crearProductoConsulta()));

    mockMvc.perform(get("/api/v1/productos")
            .param("categoria", "Frutas")
            .param("municipio", "Palmira"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.productos[0].id").value(10))
        .andExpect(jsonPath("$.productos[0].nombre").value("Aguacate"))
        .andExpect(jsonPath("$.productos[0].categoria").value("Frutas"))
        .andExpect(jsonPath("$.productos[0].cantidad").value(50))
        .andExpect(jsonPath("$.productos[0].precio").value(3500))
        .andExpect(jsonPath("$.productos[0].fechaCosecha").exists())
        .andExpect(jsonPath("$.productos[0].agricultorId").value(1))
        .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.nullValue()));
  }

  @Test
  void debeRequerirCategoriaYMunicipioParaFiltrar() throws Exception {
    mockMvc.perform(get("/api/v1/productos").param("categoria", "Frutas"))
        .andExpect(status().isBadRequest());
    mockMvc.perform(get("/api/v1/productos").param("municipio", "Palmira"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void debeMostrarMensajeCuandoNoHayProductosDisponibles() throws Exception {
    when(productoService.buscarDisponiblesPorMunicipioYCategoria("Palmira", "Frutas"))
        .thenReturn(List.of());

    mockMvc.perform(get("/api/v1/productos")
            .param("categoria", "Frutas")
            .param("municipio", "Palmira"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.productos").isEmpty())
        .andExpect(jsonPath("$.mensaje")
            .value("No hay proveedores del producto en ese municipio."));
  }
}
