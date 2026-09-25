# Documentación técnica — tienda-eventos

API REST Event-Driven con Spring Boot (Java 21) · Investigación Aplicada 2 (DWF404)

---

## 1. Objetivo de la aplicación

Es una API REST que gestiona un **CRUD de productos** (crear, listar, obtener, actualizar,
eliminar) y que, además de guardar los datos en una base H2, **reacciona a lo que pasa**
publicando y escuchando **eventos internos de Spring**. Es decir: cada vez que se crea,
actualiza o elimina un producto, el sistema "avisa" a otras partes de la aplicación de que
eso ocurrió, y esas partes reaccionan **en segundo plano**, sin que el usuario que hizo la
petición HTTP tenga que esperar a que esas tareas terminen.

Ese "avisar y reaccionar de forma desacoplada y asíncrona" es exactamente el paradigma
**Event-Driven** que pide la guía de la investigación.

---

## 2. Estructura del proyecto

```
tienda-eventos/
├── pom.xml
├── Dockerfile
├── k8s/
│   ├── 01-configmap.yaml
│   ├── 02-secret.yaml
│   ├── 03-deployment.yaml
│   └── 04-service.yaml
└── src/main/java/com/udb/tienda/
    ├── TiendaEventosApplication.java     -> arranque de la app, habilita @Async
    ├── config/
    │   └── ConfiguracionAsincrona.java   -> define el pool de hilos para los eventos
    ├── modelo/
    │   └── Producto.java                 -> entidad JPA (tabla "productos")
    ├── repositorio/
    │   └── ProductoRepositorio.java      -> acceso a datos (extiende JpaRepository)
    ├── servicio/
    │   └── ProductoServicio.java         -> logica de negocio + PUBLICA eventos
    ├── controlador/
    │   └── ProductoControlador.java      -> expone los endpoints REST
    ├── eventos/
    │   ├── ProductoCreadoEvento.java
    │   ├── ProductoActualizadoEvento.java
    │   ├── ProductoEliminadoEvento.java
    │   └── ProductoEventoListener.java   -> ESCUCHA los eventos (consumidor)
    ├── excepcion/
    │   ├── RecursoNoEncontradoExcepcion.java
    │   └── ManejadorGlobalExcepciones.java -> traduce excepciones a 400/404/500
    └── dto/
        └── RespuestaError.java           -> formato estandar de error
```

Cada carpeta representa una capa con una sola responsabilidad, lo cual facilita explicar
"quién hace qué" durante la defensa.

---

## 3. El patrón Event-Driven explicado con este código

En Spring, el mecanismo de eventos internos tiene tres piezas. Aquí están, una por una:

### 3.1 El evento (el "mensaje")
Son clases simples (`record`) dentro de `eventos/`: `ProductoCreadoEvento`,
`ProductoActualizadoEvento`, `ProductoEliminadoEvento`. Solo cargan la información
necesaria (el producto o su id). No tienen lógica, son solo datos.

### 3.2 El productor / emisor (`ProductoServicio`)
`ProductoServicio` inyecta un `ApplicationEventPublisher` (una clase que Spring provee
automáticamente). Después de guardar, actualizar o borrar en la base de datos, llama a:

```java
publicadorEventos.publishEvent(new ProductoCreadoEvento(guardado));
```

Esto es todo lo que el servicio necesita hacer. **No sabe ni le importa** quién va a
recibir ese evento ni qué hará con él — ahí está el desacoplamiento: si en el futuro se
agregan 5 listeners nuevos (por ejemplo, para enviar un webhook, actualizar una caché,
generar un log de auditoría), `ProductoServicio` **no cambia ni una línea**.

### 3.3 El consumidor / listener (`ProductoEventoListener`)
Esta clase tiene un método por cada tipo de evento, marcado con `@EventListener`. Spring
detecta automáticamente qué método debe ejecutarse según el tipo de objeto publicado
(por ejemplo, un `ProductoCreadoEvento` siempre llega a `alCrearProducto(...)`).

Además, cada método está marcado con `@Async("ejecutorEventos")`. Esto le dice a Spring:
"ejecuta este método en un hilo diferente, tomado del pool `ejecutorEventos`
(definido en `ConfiguracionAsincrona`), no en el hilo que está atendiendo la petición
HTTP". Por eso el listener puede "tardarse" (simulamos 1.5 segundos con `Thread.sleep`)
sin que el cliente de la API note ninguna demora en su respuesta.

### 3.4 Flujo completo (ejemplo: crear un producto)

```
Cliente (Postman)                Controlador           Servicio                 BD (H2)         Listener (hilo aparte)
      |  POST /api/productos          |                    |                       |                    |
      |------------------------------>|                    |                       |                    |
      |                               |--crear(producto)-->|                       |                    |
      |                               |                    |--save()------------->|                    |
      |                               |                    |<--producto con id-----|                    |
      |                               |                    |--publishEvent(evento)------------------->  |
      |                               |                    |                       |   (se dispara en   |
      |                               |<--producto creado--|                       |    un hilo async)   |
      |<---- 201 Created -------------|                    |                       |   simula 1.5s +     |
      |     (respuesta inmediata)     |                    |                       |   log "notificado"  |
```

El punto clave: la respuesta `201 Created` llega al cliente **de inmediato**, sin
esperar el `Thread.sleep(1500)` del listener, porque ese código corre en otro hilo.
Esto es justamente la ventaja de Event-Driven sobre un modelo tradicional
"thread-based" bloqueante: la capacidad de respuesta no se ve afectada por tareas
secundarias.

---

## 4. Manejo de errores (400 / 404 / 500)

Todo pasa por `ManejadorGlobalExcepciones` (`@RestControllerAdvice`), que intercepta
las excepciones lanzadas en cualquier controlador y las convierte a una respuesta JSON
estándar (`RespuestaError`), con fecha, código, tipo de error, mensaje y detalles:

| Código | Cuándo ocurre | Excepción capturada |
|--------|---------------|----------------------|
| 400 | El body no cumple las validaciones (`@NotBlank`, `@NotNull`, `@Positive` en `Producto`) | `MethodArgumentNotValidException` |
| 404 | Se pide/actualiza/borra un producto cuyo id no existe | `RecursoNoEncontradoExcepcion` (lanzada desde `ProductoServicio.buscarPorId`) |
| 500 | Cualquier error no previsto | `Exception` (genérico) |

Ejemplo de respuesta 404:
```json
{
  "fecha": "2026-09-21T10:00:00",
  "estado": 404,
  "error": "No encontrado",
  "mensaje": "No existe un producto con id 99",
  "detalles": []
}
```

---

## 5. Persistencia con H2

- `spring.datasource.url=jdbc:h2:mem:tiendadb`: la base vive **en memoria**, se crea
  vacía cada vez que arranca la app.
- `spring.jpa.hibernate.ddl-auto=update`: Hibernate crea la tabla `productos`
  automáticamente a partir de la clase `Producto` (no hay que escribir SQL a mano).
- Consola visual disponible en `http://localhost:8080/h2-console` para mostrar en el
  video demostrativo que los datos sí se guardan.

---

## 6. Empaquetado en Docker

El `Dockerfile` usa **multi-stage build**:

1. **Etapa `build`**: usa una imagen con Maven + JDK 21 para compilar el proyecto y
   generar el `.jar` (`mvn clean package`).
2. **Etapa final**: usa una imagen liviana (`eclipse-temurin:21-jre-alpine`, solo el
   *runtime* de Java, sin Maven ni código fuente) y copia únicamente el `.jar` ya
   compilado.

Esto genera una imagen final mucho más pequeña y segura que si se compilara y corriera
todo en la misma imagen.

```bash
docker build -t tienda-eventos:1.0.0 .
docker run -p 8080:8080 tienda-eventos:1.0.0
```

---

## 7. Despliegue en Kubernetes

Los manifiestos en `k8s/` se aplican en este orden:

1. **`01-configmap.yaml`**: variables de configuración no sensibles (ej. el nombre de
   la app), inyectadas como variables de entorno. Permite cambiar configuración sin
   reconstruir la imagen.
2. **`02-secret.yaml`**: datos sensibles (ejemplo: una llave de API de notificaciones),
   separados del código y de la imagen Docker.
3. **`03-deployment.yaml`**: define **2 réplicas** del pod (alta disponibilidad), y dos
   *probes*:
   - **`readinessProbe`**: Kubernetes no envía tráfico al pod hasta que
     `/actuator/health/readiness` responda OK (la app ya inició por completo).
   - **`livenessProbe`**: si `/actuator/health/liveness` deja de responder,
     Kubernetes reinicia el pod automáticamente (autorecuperación).
   Ambos endpoints los expone la dependencia `spring-boot-starter-actuator`.
4. **`04-service.yaml`**: expone los pods como un único punto de acceso (`NodePort`),
   balanceando las peticiones entre las réplicas.

```bash
kubectl apply -f k8s/
kubectl get pods
kubectl get svc
```

---

## 8. Cómo probar la API (Postman / curl)

**Crear producto**
```
POST http://localhost:8080/api/productos
Content-Type: application/json

{
  "nombre": "Mouse inalámbrico",
  "descripcion": "2.4GHz, batería AA",
  "precio": 15.5,
  "stock": 100
}
```
Respuesta: `201 Created` con el producto (incluyendo su `id`), y en la consola/logs del
servidor aparece ~1.5s después el mensaje del listener asíncrono confirmando que el
evento fue procesado.

**Listar productos:** `GET /api/productos`
**Obtener uno:** `GET /api/productos/1`
**Actualizar:** `PUT /api/productos/1` (mismo body que crear)
**Eliminar:** `DELETE /api/productos/1` → `204 No Content`

**Probar error 404:** `GET /api/productos/9999`
**Probar error 400:** `POST /api/productos` con `{"nombre": "", "precio": -5}`

---

## 9. Resumen para la defensa (preguntas típicas)

- **¿Dónde se aplica Event-Driven?** En `ProductoServicio` (publica eventos con
  `ApplicationEventPublisher`) y en `ProductoEventoListener` (los consume con
  `@EventListener`).
- **¿Por qué es asíncrono?** Porque los métodos del listener tienen `@Async`, así que
  corren en un hilo del pool `ejecutorEventos` y no bloquean la respuesta HTTP.
- **¿Qué pasa si falla el listener?** No afecta la respuesta HTTP (ya se envió), pero
  quedaría registrado en los logs; en un sistema productivo se agregarían reintentos o
  una cola de mensajería (RabbitMQ/Kafka) para garantizar la entrega.
- **¿Cómo se garantiza la escalabilidad en Kubernetes?** Con 2 réplicas del Deployment
  y los `readiness/liveness probes`, que permiten que el clúster reemplace pods caídos
  y no envíe tráfico a instancias que aún no están listas.
