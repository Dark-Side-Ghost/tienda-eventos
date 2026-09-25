package com.udb.tienda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Clase principal de la aplicacion.
 *
 * @EnableAsync habilita la ejecucion de metodos marcados con @Async
 * en hilos separados (necesario para el paradigma Event-Driven,
 * ya que los listeners de eventos se ejecutan en segundo plano).
 */
@SpringBootApplication
@EnableAsync
public class TiendaEventosApplication {

    public static void main(String[] args) {
        SpringApplication.run(TiendaEventosApplication.class, args);
    }
}
