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
docker build -t TU_USUARIO/tienda-eventos:1.0.0 .
docker run -p 8080:8080 TU_USUARIO/tienda-eventos:1.0.0
```

## Desplegar en Kubernetes

```bash
# 1. Publicar la imagen en un registro (Docker Hub, GHCR, etc.)
docker push TU_USUARIO/tienda-eventos:1.0.0

# 2. Editar k8s/03-deployment.yaml y poner el nombre real de la imagen

# 3. Aplicar los manifiestos
kubectl apply -f k8s/01-configmap.yaml
kubectl apply -f k8s/02-secret.yaml
kubectl apply -f k8s/03-deployment.yaml
kubectl apply -f k8s/04-service.yaml

# 4. Ver el estado
kubectl get pods
kubectl get svc tienda-eventos-service
```

## Licencia
Proyecto académico - Universidad Don Bosco - Investigación Aplicada 2 (DWF404).
