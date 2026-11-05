package com.agrovalle.connect.dto;

/**
 * Representa la respuesta de inicio de sesión de un agricultor, incluyendo el token JWT.
 */
public record LoginResponse(String token, String tipo, long expiraEnMs) {
}
