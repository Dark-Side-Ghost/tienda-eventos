package com.udb.tienda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configura el "pool" de hilos que usara Spring para ejecutar
 * los metodos anotados con @Async (nuestros listeners de eventos).
 *
 * Sin este bean, Spring usaria un executor simple por defecto;
 * aqui lo definimos explicitamente para poder explicarlo en la
 * defensa y controlar cuantos hilos se usan.
 */
@Configuration
public class ConfiguracionAsincrona {

    @Bean(name = "ejecutorEventos")
    public TaskExecutor ejecutorEventos() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("hilo-evento-");
        executor.initialize();
        return executor;
    }
}
