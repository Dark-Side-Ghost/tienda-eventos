package com.udb.tienda.eventos;

import com.udb.tienda.modelo.Producto;

/**
 * Evento que se publica cuando un producto existente es actualizado.
 */
public record ProductoActualizadoEvento(Producto producto) {
}
