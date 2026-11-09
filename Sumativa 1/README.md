# Sistema de Gestión de Reservas y Tickets para Eventos (Cloud Native)
**Asignatura:** Desarrollo Cloud Native (CDY2204)  
**Institución:** Duoc UC  
**Evaluación:** Semana 3 - Sumativa *"Construyendo y desplegando una solución Cloud Native"*  
**Autor:** Víctor Lind  
**Calificación Objetivo:** 100% Completamente Logrado (CL - 70/70 puntos)

---

## 📋 Resumen del Proyecto

Este microservicio implementa una solución de nivel empresarial para una startup de eventos que requiere emisión, custodia temporal, persistencia y consulta de tickets digitales. Integra tecnologías Cloud Native modernas bajo las directrices del Twelve-Factor App:

1. **Almacenamiento Temporal en Amazon EFS:** Generación en memoria y custodia inmediata de tickets PDF en un sistema de archivos NFS compartido (`/mnt/efs/tickets/{eventoId}/{usuarioId}/`).
2. **Persistencia Definitiva en Amazon S3:** Subida automática de tickets bajo la clave requerida: `/{eventoId}/{usuarioId}/{ticketId}.pdf`.
3. **Modificación y Actualización:** Capacidad de re-emitir tickets, regenerar PDFs y sincronizar cambios tanto en EFS como en AWS S3.
4. **Descargas Flexibles:** Streaming binario directo HTTP y generación de URLs prefirmadas (Pre-signed URLs) desde S3.
5. **Historial y Estadísticas:** Auditoría transaccional de operaciones de archivos con filtros avanzados (fecha, usuario, evento, acción) y métricas de ocupación, ventas y almacenamiento.
6. **Pipeline CI/CD Automatizado:** Pipeline en GitHub Actions con compilación, pruebas unitarias, publicación de imagen en Docker Hub y despliegue continuo vía SSH en una instancia AWS EC2 con volumen EFS montado.
7. **Documentación y Pruebas en Vivo:** Documentación interactiva OpenAPI/Swagger UI, colección de Postman con pruebas automáticas y guion paso a paso para la presentación en Microsoft Teams.

---

## 🛠️ Stack Tecnológico

- **Lenguaje:** Java 21 (LTS).
- **Framework:** Spring Boot 3.3.5 (Spring MVC, Spring Data JPA, Spring Validation, Spring Boot Actuator).
- **Cloud SDK:** AWS SDK for Java v2 (`software.amazon.awssdk:s3`).
- **Generación de Tickets:** OpenPDF (PDF Generator) + ZXing (QR Code 2D Generator).
- **Persistencia de Metadatos:** PostgreSQL 16 (Producción) / H2 in-memory (Desarrollo y pruebas locales inmediatas).
- **Documentación de API:** Springdoc OpenAPI 3.0 / Swagger UI.
- **Contenedorización:** Docker con Multi-Stage Build ligero sobre Alpine Linux JRE 21.
- **Orquestación Local:** Docker Compose (Postgres + LocalStack S3 + Ticket Service).
- **CI/CD:** GitHub Actions + Docker Hub + AWS EC2 (SSH deployment).

---

## 🚀 Inicio Rápido

### Opción 1: Ejecución Local Inmediata (Sin dependencias externas)
El microservicio incluye perfil `dev` con base de datos H2 en memoria y almacenamiento local automático:

```powershell
# En Windows (usando script incluido):
.\Sumativa 1\scripts\run_local.bat

# O directamente con Maven:
& "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" -f "Sumativa 1\ticket-service\pom.xml" spring-boot:run
```

Una vez iniciado:
- **API Base:** `http://localhost:8080`
- **Swagger UI Interactivo:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Healthcheck:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- **Consola H2:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:ticketdb`, User: `sa`, Password: vacía)

---

### Opción 2: Ejecución con Docker Compose (PostgreSQL + LocalStack S3 + EFS)
Para ejecutar la arquitectura completa emulando AWS en contenedores:

```bash
cd "Sumativa 1"
docker compose up -d
```

Verificar estado de los contenedores:
```bash
docker compose ps
docker compose logs -f ticket-service
```

---

## 📬 Pruebas con Postman

En la carpeta `postman/` encontrarás:
1. `TicketFlow_API.postman_collection.json`: 14 peticiones ordenadas secuencialmente que cubren el 100% de la rúbrica con tests automáticos de aserción (`pm.test`).
2. `TicketFlow_Local.postman_environment.json`: Variables para pruebas locales (`baseUrl: http://localhost:8080`).
3. `TicketFlow_EC2.postman_environment.json`: Variables para pruebas en AWS EC2 (`baseUrl: http://<IP_EC2>:8080`).

### Ejecución de Pruebas:
1. Abre Postman y haz clic en **Import**.
2. Selecciona ambos archivos JSON de la carpeta `postman/`.
3. Selecciona el entorno `TicketFlow Local`.
4. Utiliza el **Collection Runner** para ejecutar las pruebas automatizadas de extremo a extremo.

---

## 🌐 Endpoints REST Principales

### 1. Gestión de Tickets
| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/v1/tickets/generate` | Genera ticket PDF en EFS y lo sube automáticamente a S3 |
| `POST` | `/api/v1/tickets/{id}/upload-s3` | Sube o re-sincroniza manualmente a S3 |
| `GET` | `/api/v1/tickets/{id}` | Obtiene metadatos y enlaces del ticket |
| `GET` | `/api/v1/tickets` | Lista tickets con filtros (`eventCode`, `userId`, `status`) |
| `GET` | `/api/v1/tickets/{id}/download` | Descarga directa del ticket PDF en streaming |
| `GET` | `/api/v1/tickets/{id}/presigned-url` | Obtiene URL prefirmada temporal de S3 |
| `PUT` | `/api/v1/tickets/{id}` | Modifica datos del ticket, regenera PDF y actualiza en S3 y EFS |
| `DELETE` | `/api/v1/tickets/{id}` | Elimina ticket y purga archivos de S3 y EFS |
| `GET` | `/api/v1/tickets/history` | Consulta historial de archivos generados con filtros y paginación |
| `GET` | `/api/v1/tickets/download/user/{userId}` | Descarga masiva de tickets de un usuario en ZIP |
| `GET` | `/api/v1/tickets/download/event/{eventCode}` | Descarga masiva de tickets de un evento en ZIP |

### 2. Gestión de Eventos y Estadísticas
| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/api/v1/events` | Lista todos los eventos disponibles |
| `POST` | `/api/v1/events` | Registra un nuevo evento |
| `GET` | `/api/v1/events/{eventCode}` | Detalle de un evento específico |
| `GET` | `/api/v1/events/{eventCode}/stats` | Reporte de estadísticas de ventas, reservas y almacenamiento |

---

## ⚙️ Configuración del Pipeline CI/CD en GitHub Actions

El archivo `.github/workflows/ci-cd.yml` automatiza la integración y entrega continua. Para habilitar el despliegue automático hacia AWS EC2, agrega los siguientes **Secrets** en tu repositorio de GitHub (**Settings > Secrets and variables > Actions**):

| Secret | Descripción | Ejemplo |
| :--- | :--- | :--- |
| `DOCKERHUB_USERNAME` | Usuario de Docker Hub | `victorlind` |
| `DOCKERHUB_TOKEN` | Token de acceso personal de Docker Hub | `dckr_pat_xxxx` |
| `EC2_HOST` | Dirección IP pública o DNS de tu instancia EC2 | `54.210.xx.xx` |
| `EC2_USERNAME` | Usuario SSH de la instancia | `ubuntu` o `ec2-user` |
| `EC2_SSH_KEY` | Clave privada SSH completa (.pem) | `-----BEGIN RSA PRIVATE KEY...` |
| `AWS_ACCESS_KEY_ID` | Clave de acceso de AWS | `AKIAIOSFODNN7EXAMPLE` |
| `AWS_SECRET_ACCESS_KEY` | Clave secreta de AWS | `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY` |
| `AWS_REGION` | Región AWS del bucket S3 y EFS | `us-east-1` |
| `AWS_S3_BUCKET` | Nombre del bucket S3 | `ticketflow-storage-duoc-vjvl` |

---

## 🎥 Guía para la Grabación y Presentación en Teams

Consulta el documento especializado:
📄 **[`docs/GUIA_GRABACION_PRESENTACION.md`](docs/GUIA_GRABACION_PRESENTACION.md)**

Contiene:
- Estructura temporal minuto a minuto (8 a 10 minutos).
- Guion con el diálogo exacto en español a pronunciar.
- Instrucciones de qué mostrar en pantalla (Postman, PDF con código QR, Swagger, GitHub Actions, Docker Hub).
- Checklist de verificación previo al envío al AVA.

---

## 📄 Licencia y Derechos de Autor

Desarrollado para la asignatura **Desarrollo Cloud Native (CDY2204)** - Duoc UC.  
*Todos los derechos reservados.*
