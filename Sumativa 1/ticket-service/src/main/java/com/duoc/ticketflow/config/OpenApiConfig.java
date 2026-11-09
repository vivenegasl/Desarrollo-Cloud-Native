package com.duoc.ticketflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TicketFlow Cloud Native API - Duoc UC")
                        .version("1.0.0")
                        .description("Microservicio de Gestión de Reservas y Tickets para Eventos.\n\n" +
                                "**Características Cloud Native:**\n" +
                                "- Almacenamiento temporal en Amazon EFS (/mnt/efs/tickets)\n" +
                                "- Persistencia automática, modificación y streaming de descarga en Amazon S3\n" +
                                "- Generación dinámica de tickets en PDF con código QR y checksum SHA-256\n" +
                                "- Historial completo de operaciones y reporte de estadísticas de ventas y reservas\n" +
                                "- Automatización CI/CD con Docker Hub y despliegue en AWS EC2")
                        .contact(new Contact()
                                .name("Víctor Lind - Desarrollo Cloud Native (CDY2204)")
                                .email("victor.lind@duocuc.cl"))
                        .license(new License().name("Apache 2.0").url("https://spring.io/")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Entorno Local"),
                        new Server().url("http://ec2-instance:8080").description("Instancia AWS EC2 Producción")
                ));
    }
}
