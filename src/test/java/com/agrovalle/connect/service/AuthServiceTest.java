package com.agrovalle.connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.agrovalle.connect.dto.LoginRequest;
import com.agrovalle.connect.dto.LoginResponse;
import com.agrovalle.connect.exception.CredencialesInvalidasException;
import com.agrovalle.connect.model.Usuario;
import com.agrovalle.connect.model.enums.Rol;
import com.agrovalle.connect.repository.UsuarioRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Usuario crearUsuarioConHash() {
        Usuario usuario = new Usuario(
                "1111078900", "juan@correo.com", "hash-guardado", Rol.AGRICULTOR);
        usuario.setId(1L);
        return usuario;
    }

    @Test
    void debeLoguearConCredencialesValidas() {
        Usuario usuario = crearUsuarioConHash();
        when(usuarioRepository.findByIdentificacion("1111078900"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("contrasena123", "hash-guardado")).thenReturn(true);
        when(jwtService.generarToken(1L, Set.of(Rol.AGRICULTOR))).thenReturn("token-simulado");
        when(jwtService.getExpiracionMs()).thenReturn(3600000L);

        LoginResponse respuesta = authService.login(
                new LoginRequest("1111078900", "contrasena123"));

        assertEquals("token-simulado", respuesta.token());
        assertEquals("Bearer", respuesta.tipo());
    }

    @Test
    void debeRechazarContrasenaIncorrecta() {
        Usuario usuario = crearUsuarioConHash();
        when(usuarioRepository.findByIdentificacion("1111078900"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("contrasena-incorrecta", "hash-guardado")).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(new LoginRequest("1111078900", "contrasena-incorrecta")));
    }

    @Test
    void debeRechazarIdentificacionInexistente() {
        when(usuarioRepository.findByIdentificacion("0000000000"))
                .thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(new LoginRequest("0000000000", "cualquiera")));
    }

    @Test
    void debeRechazarUsuarioInactivo() {
        Usuario usuario = crearUsuarioConHash();
        usuario.setActivo(false);
        when(usuarioRepository.findByIdentificacion("1111078900"))
                .thenReturn(Optional.of(usuario));

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(new LoginRequest("1111078900", "contrasena123")));
    }
}