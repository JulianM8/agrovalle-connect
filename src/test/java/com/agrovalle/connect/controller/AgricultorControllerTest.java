package com.agrovalle.connect.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalle.connect.config.SecurityConfig;
import com.agrovalle.connect.dto.AgricultorRegistroRequest;
import com.agrovalle.connect.dto.AgricultorRegistroResponse;
import com.agrovalle.connect.dto.LoginRequest;
import com.agrovalle.connect.dto.LoginResponse;
import com.agrovalle.connect.exception.CredencialesInvalidasException;
import com.agrovalle.connect.exception.GlobalExceptionHandler;
import com.agrovalle.connect.exception.IdentificacionDuplicadaException;
import com.agrovalle.connect.service.AgricultorService;
import com.agrovalle.connect.service.AuthService;
import com.agrovalle.connect.service.JwtService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AgricultorController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
class AgricultorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AgricultorService agricultorService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    private AgricultorRegistroRequest crearRequestValido() {
        return new AgricultorRegistroRequest(
                "Juan", "Perez", "1111078900", "Palmira", "3001234567", "juan@correo.com", "contrasena123");
    }

    @Test
    void debeRegistrarAgricultorConDatosValidos() throws Exception {
        AgricultorRegistroResponse respuesta = new AgricultorRegistroResponse(
                1L, "Juan", "Perez", "1111078900", "Palmira", "Registro realizado correctamente");
        when(agricultorService.registrar(any(AgricultorRegistroRequest.class)))
                .thenReturn(respuesta);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Registro realizado correctamente"));

    }

    @Test
    void debeRechazarDatosIncompletos() throws Exception {
        AgricultorRegistroRequest requestInvalido = new AgricultorRegistroRequest(
                "", "Perez", "1111078900", "Palmira", "3001234567",
                "juan@correo.com", "contrasena123");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void debeRechazarIdentificacionConMenosDe10Digitos() throws Exception {
        AgricultorRegistroRequest requestInvalido = new AgricultorRegistroRequest(
                "Juan", "Perez", "123456", "Palmira", "3001234567",
                "juan@correo.com", "contrasena123");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void debeRechazarIdentificacionDuplicada() throws Exception {
        when(agricultorService.registrar(any(AgricultorRegistroRequest.class)))
                .thenThrow(new IdentificacionDuplicadaException("1111078900"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andExpect(status().isConflict());
    }

    @Test
    void debeLoguearConCredencialesValidas() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("token-simulado", "Bearer", 3600000L));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new LoginRequest("1111078900", "contrasena123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-simulado"));
    }

    @Test
    void debeRechazarCredencialesInvalidas() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new CredencialesInvalidasException());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new LoginRequest("1111078900", "contrasena-incorrecta"))))
                .andExpect(status().isUnauthorized());
    }
}
