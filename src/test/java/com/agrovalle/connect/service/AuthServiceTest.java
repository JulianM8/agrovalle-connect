package com.agrovalle.connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.agrovalle.connect.dto.LoginRequest;
import com.agrovalle.connect.dto.LoginResponse;
import com.agrovalle.connect.exception.CredencialesInvalidasException;
import com.agrovalle.connect.model.Agricultor;
import com.agrovalle.connect.repository.AgricultorRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AgricultorRepository agricultorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Agricultor crearAgricultorConHash() {
        Agricultor agricultor = new Agricultor(
                "Juan", "Perez", "1111078900", "Palmira", "3001234567",
                "juan@correo.com", "hash-guardado");
        agricultor.setId(1L);
        return agricultor;
    }

    @Test
    void debeLoguearConCredencialesValidas() {
        Agricultor agricultor = crearAgricultorConHash();
        when(agricultorRepository.findByIdentificacion("1111078900"))
                .thenReturn(Optional.of(agricultor));
        when(passwordEncoder.matches("contrasena123", "hash-guardado")).thenReturn(true);
        when(jwtService.generarToken(1L)).thenReturn("token-simulado");
        when(jwtService.getExpiracionMs()).thenReturn(3600000L);

        LoginResponse respuesta = authService.login(
                new LoginRequest("1111078900", "contrasena123"));

        assertEquals("token-simulado", respuesta.token());
        assertEquals("Bearer", respuesta.tipo());
    }

    @Test
    void debeRechazarContrasenaIncorrecta() {
        Agricultor agricultor = crearAgricultorConHash();
        when(agricultorRepository.findByIdentificacion("1111078900"))
                .thenReturn(Optional.of(agricultor));
        when(passwordEncoder.matches("contrasena-incorrecta", "hash-guardado")).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(new LoginRequest("1111078900", "contrasena-incorrecta")));
    }

    @Test
    void debeRechazarIdentificacionInexistente() {
        when(agricultorRepository.findByIdentificacion("0000000000"))
                .thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(new LoginRequest("0000000000", "cualquiera")));
    }
}
