package com.udb.tienda.excepcion;

/**
 * Se lanza cuando se busca un producto por id y este no existe.
 * El manejador global la traduce a un HTTP 404.
 */
public class RecursoNoEncontradoExcepcion extends RuntimeException {
    public RecursoNoEncontradoExcepcion(String mensaje) {
        super(mensaje);
    }
}
