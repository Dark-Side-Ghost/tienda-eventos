package com.udb.tienda.eventos;

/**
 * Evento que se publica cuando se elimina un producto.
 * Solo necesitamos el id, ya que el producto ya no existe
 * en la base de datos al momento de notificar.
 */
public record ProductoEliminadoEvento(Long productoId) {
}
