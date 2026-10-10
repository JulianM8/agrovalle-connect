package com.agrovalle.connect.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import com.agrovalle.connect.service.JwtService;
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
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
class ProductoControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private ProductoService productoService;

        @MockitoBean
        private JwtService jwtService;

        private ProductoPublicacionRequest crearRequestValido() {
                return new ProductoPublicacionRequest(
                                "Aguacate", "Frutas", 50, new BigDecimal("3500"), LocalDate.now().plusDays(5));
        }

        @Test
        void debePublicarProductoConTokenValido() throws Exception {
                when(jwtService.extraerAgricultorId(eq("token-valido"))).thenReturn(1L);
                ProductoPublicacionResponse respuesta = new ProductoPublicacionResponse(
                                10L, "Aguacate", "Frutas", 50, new BigDecimal("3500"),
                                LocalDate.now().plusDays(5), 1L, "Producto publicado correctamente");
                when(productoService.publicar(any(ProductoPublicacionRequest.class), eq(1L)))
                                .thenReturn(respuesta);

                mockMvc.perform(post("/api/v1/productos")
                                .header("Authorization", "Bearer token-valido")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(crearRequestValido())))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.mensaje").value("Producto publicado correctamente"));
        }

        @Test
        void debeRechazarPeticionSinToken() throws Exception {
                mockMvc.perform(post("/api/v1/productos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(crearRequestValido())))
                                .andExpect(status().isForbidden());
        }

        @Test
        void debeRechazarTokenInvalido() throws Exception {
                when(jwtService.extraerAgricultorId(anyString()))
                                .thenThrow(new io.jsonwebtoken.JwtException("Token inválido o expirado"));

                mockMvc.perform(post("/api/v1/productos")
                                .header("Authorization", "Bearer token-invalido")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(crearRequestValido())))
                                .andExpect(status().isForbidden());
        }

        @Test
        void debeRechazarFechaDeCosechaPasada() throws Exception {
                when(jwtService.extraerAgricultorId(eq("token-valido"))).thenReturn(1L);
                when(productoService.publicar(any(ProductoPublicacionRequest.class), anyLong()))
                                .thenThrow(new FechaCosechaInvalidaException(LocalDate.now().minusDays(1)));

                mockMvc.perform(post("/api/v1/productos")
                                .header("Authorization", "Bearer token-valido")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(crearRequestValido())))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void debeRechazarAgricultorInexistente() throws Exception {
                when(jwtService.extraerAgricultorId(eq("token-valido"))).thenReturn(99L);
                when(productoService.publicar(any(ProductoPublicacionRequest.class), eq(99L)))
                                .thenThrow(new AgricultorNoEncontradoException(99L));

                mockMvc.perform(post("/api/v1/productos")
                                .header("Authorization", "Bearer token-valido")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(crearRequestValido())))
                                .andExpect(status().isNotFound());
        }
}
