package com.agrovalle.connect.dto;

public record LoginResponse(String token, String tipo, long expiraEnMs) {
}
