# ==============================================================================
# Dockerfile Multi-Stage Build Optimizado para TicketFlow Service
# Asignatura: Desarrollo Cloud Native (CDY2204) - Duoc UC
# ==============================================================================

# Stage 1: Build y compilación limpia con Maven (sin descargas redundantes)
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime ligero, seguro y mínimo con Java 21 JRE
FROM eclipse-temurin:21-jre-alpine AS runner

LABEL maintainer="Victor Lind <victor.lind@duocuc.cl>"
LABEL description="Microservicio de Gestión de Reservas y Tickets - AWS Cloud Native"
LABEL version="1.0.0"

# Instalar curl para healthchecks
RUN apk add --no-cache curl tzdata

# Crear usuario y grupo de aplicación sin privilegios root (Seguridad Cloud Native)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Crear directorio de montaje de Amazon EFS para almacenamiento temporal (Criterio 1)
RUN mkdir -p /mnt/efs/tickets /app && \
    chown -R appuser:appgroup /mnt/efs/tickets /app

WORKDIR /app

# Copiar el artefacto JAR final
COPY --from=builder --chown=appuser:appgroup /build/target/ticket-service-1.0.0.jar app.jar

USER appuser

# Variables de entorno Cloud Native
ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    EFS_MOUNT_PATH=/mnt/efs/tickets \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

# Health Check nativo de Docker hacia Spring Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
