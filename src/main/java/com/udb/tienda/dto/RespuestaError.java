package com.udb.tienda.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Estructura estandar para todas las respuestas de error de la API,
 * para que el cliente siempre reciba el mismo formato sin importar
 * el tipo de error (400, 404, 500).
 */
public record RespuestaError(
        LocalDateTime fecha,
        int estado,
        String error,
        String mensaje,
        List<String> detalles
) {
    public RespuestaError(int estado, String error, String mensaje, List<String> detalles) {
        this(LocalDateTime.now(), estado, error, mensaje, detalles);
    }
}
