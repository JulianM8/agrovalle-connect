package com.agrovalle.connect.controller;
 
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
import com.agrovalle.connect.config.SecurityConfig;
import com.agrovalle.connect.dto.ProductoPublicacionRequest;
import com.agrovalle.connect.dto.ProductoPublicacionResponse;
import com.agrovalle.connect.exception.AgricultorNoEncontradoException;
import com.agrovalle.connect.exception.FechaCosechaInvalidaException;
import com.agrovalle.connect.exception.GlobalExceptionHandler;
import com.agrovalle.connect.service.ProductoService;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
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
}
