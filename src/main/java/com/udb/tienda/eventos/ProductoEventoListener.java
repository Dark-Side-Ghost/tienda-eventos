package com.udb.tienda.eventos;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Este componente es el "consumidor" (listener) de los eventos
 * publicados por ProductoServicio.
 *
 * Cada metodo:
 *  - Esta anotado con @EventListener: Spring lo invoca automaticamente
 *    cuando alguien publica el evento correspondiente (ApplicationEventPublisher).
 *  - Esta anotado con @Async: se ejecuta en un hilo distinto al hilo
 *    que atendio la peticion HTTP. Esto significa que el cliente de la
 *    API REST recibe su respuesta (201, 200, 204...) de inmediato,
 *    sin esperar a que termine esta tarea de fondo (background task).
 *
 * Aqui simulamos tareas tipicas de un sistema orientado a eventos:
 * enviar una notificacion/correo y actualizar un sistema externo.
 * En un caso real, este mismo listener podria publicar el evento
 * hacia RabbitMQ o Kafka en lugar de solo loguearlo.
 */
@Component
@Slf4j
public class ProductoEventoListener {

    @Async("ejecutorEventos")
    @EventListener
    public void alCrearProducto(ProductoCreadoEvento evento) {
        simularTrabajoDeFondo();
        log.info("[EVENTO][hilo={}] Producto creado -> id={}, nombre='{}'. " +
                        "Se simula el envio de una notificacion de nuevo producto.",
                Thread.currentThread().getName(),
                evento.producto().getId(),
                evento.producto().getNombre());
    }

    @Async("ejecutorEventos")
    @EventListener
    public void alActualizarProducto(ProductoActualizadoEvento evento) {
        simularTrabajoDeFondo();
        log.info("[EVENTO][hilo={}] Producto actualizado -> id={}, nombre='{}'. " +
                        "Se simula la sincronizacion con un sistema externo de inventario.",
                Thread.currentThread().getName(),
                evento.producto().getId(),
                evento.producto().getNombre());
    }

    @Async("ejecutorEventos")
    @EventListener
    public void alEliminarProducto(ProductoEliminadoEvento evento) {
        simularTrabajoDeFondo();
        log.info("[EVENTO][hilo={}] Producto eliminado -> id={}. " +
                        "Se simula la limpieza de referencias en otros modulos.",
                Thread.currentThread().getName(),
                evento.productoId());
    }

    /**
     * Simula una tarea que toma tiempo (por ejemplo, una llamada
     * a un servicio externo o el envio de un correo), para que en
     * la demostracion sea evidente que esto ocurre en segundo plano
     * y no bloquea la respuesta HTTP.
     */
    private void simularTrabajoDeFondo() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
