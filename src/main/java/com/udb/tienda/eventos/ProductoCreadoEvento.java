package com.udb.tienda.eventos;

import com.udb.tienda.modelo.Producto;

/**
 * Evento que se publica cuando se crea un producto nuevo.
 * Es un simple POJO (record) que viaja con la informacion
 * necesaria para que los listeners reaccionen sin necesidad
 * de volver a consultar la base de datos.
 */
public record ProductoCreadoEvento(Producto producto) {
}
