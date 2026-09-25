# ---------- Etapa 1: Construccion ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos primero el pom.xml para aprovechar la cache de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el codigo fuente y compilamos
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- Etapa 2: Imagen final (liviana) ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos solo el jar generado en la etapa anterior
COPY --from=build /app/target/tienda-eventos.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
