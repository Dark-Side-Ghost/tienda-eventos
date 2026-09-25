package com.udb.tienda.servicio;

import com.udb.tienda.eventos.ProductoActualizadoEvento;
import com.udb.tienda.eventos.ProductoCreadoEvento;
import com.udb.tienda.eventos.ProductoEliminadoEvento;
import com.udb.tienda.excepcion.RecursoNoEncontradoExcepcion;
import com.udb.tienda.modelo.Producto;
import com.udb.tienda.repositorio.ProductoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Contiene la logica de negocio del CRUD de productos.
 *
 * Esta clase es el "productor" de eventos: despues de cada operacion
 * que modifica el estado (crear, actualizar, eliminar), usa
 * ApplicationEventPublisher para publicar un evento. Spring se encarga
 * de entregarlo a todos los @EventListener registrados
 * (ver ProductoEventoListener), de forma desacoplada: este servicio
 * no sabe ni le importa quien reacciona al evento ni que hace con el.
 */
@Service
@RequiredArgsConstructor
public class ProductoServicio {

    private final ProductoRepositorio productoRepositorio;
    private final ApplicationEventPublisher publicadorEventos;

    public List<Producto> listarTodos() {
        return productoRepositorio.findAll();
    }

    public Producto buscarPorId(Long id) {
        return productoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                        "No existe un producto con id " + id));
    }

    public Producto crear(Producto producto) {
        Producto guardado = productoRepositorio.save(producto);
        // Se publica el evento DESPUES de confirmar el guardado en BD.
        publicadorEventos.publishEvent(new ProductoCreadoEvento(guardado));
        return guardado;
    }

    public Producto actualizar(Long id, Producto datosNuevos) {
        Producto existente = buscarPorId(id);
        existente.setNombre(datosNuevos.getNombre());
        existente.setDescripcion(datosNuevos.getDescripcion());
        existente.setPrecio(datosNuevos.getPrecio());
        existente.setStock(datosNuevos.getStock());

        Producto actualizado = productoRepositorio.save(existente);
        publicadorEventos.publishEvent(new ProductoActualizadoEvento(actualizado));
        return actualizado;
    }

    public void eliminar(Long id) {
        Producto existente = buscarPorId(id);
        productoRepositorio.delete(existente);
        publicadorEventos.publishEvent(new ProductoEliminadoEvento(id));
    }
}
