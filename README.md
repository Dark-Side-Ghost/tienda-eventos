# tienda-eventos

API REST en Spring Boot (Java 21) que implementa el paradigma **Event-Driven**
mediante eventos internos de Spring (`ApplicationEventPublisher` + `@EventListener`)
y ejecución asíncrona (`@Async`), con persistencia en H2 y despliegue en Docker/Kubernetes.

> Para la explicación detallada de la arquitectura y el flujo de eventos, ver `DOCUMENTACION.md`.

## Requisitos
- Java 21
- Maven 3.9+
- Docker (opcional, para contenedores)
- kubectl + un clúster (minikube, kind, etc.) (opcional, para Kubernetes)

## Ejecutar localmente

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080/api/productos`.
Consola H2: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:tiendadb`, user: `sa`, password vacía).

## Endpoints

| Método | Ruta                    | Descripción                  |
|--------|--------------------------|-------------------------------|
| GET    | /api/productos           | Lista todos los productos     |
| GET    | /api/productos/{id}      | Obtiene un producto por id    |
| POST   | /api/productos           | Crea un producto              |
| PUT    | /api/productos/{id}      | Actualiza un producto         |
| DELETE | /api/productos/{id}      | Elimina un producto           |

### Ejemplo de body (POST/PUT)
```json
{
  "nombre": "Teclado mecanico",
  "descripcion": "Switches rojos, retroiluminado",
  "precio": 45.99,
  "stock": 20
}
```

## Ejecutar con Docker

```bash
docker build -t tienda-eventos:1.0.0 .
docker run -p 8080:8080 tienda-eventos:1.0.0
```

## Desplegar en Kubernetes

La imagen ya está publicada públicamente en Docker Hub:
[`robotman28/tienda-eventos:1.0.0`](https://hub.docker.com/r/robotman28/tienda-eventos)

Para desplegarla directamente, sin necesidad de construir nada:

```bash
kubectl apply -f k8s/
kubectl get pods
```

## Integrantes del equipo

| Nombre completo | Número de carnet |
|---|---|
| _(WILLIAM AARÓN PERALTA CRUZ)_ | _(PC210574)_ |
| _(DELMY BEATRIZ GONZÁLEZ ARAGÓN)_ | _(GA252427)_ |
| _(ROBERTO ANTONIO CABRERA NAVAS)_ | _(CN233178)_ |
| _(HELEN VERÓNICA PORTILLO RODRÍGUEZ)_ | _(PR230742)_ |

## Enlaces de la entrega

- **Repositorio:** _(https://github.com/Dark-Side-Ghost/tienda-eventos.git)_
- **Video de exposición:** _()_
- **Video demostrativo:** _()_

## Licencia

Proyecto académico - Universidad Don Bosco - Investigación Aplicada 2 (DWF404).

