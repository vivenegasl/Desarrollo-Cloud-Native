package com.duoc.ticketflow.config;

import com.duoc.ticketflow.model.Event;
import com.duoc.ticketflow.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(EventRepository eventRepository) {
        return args -> {
            if (!eventRepository.existsByEventCode("evento123")) {
                Event event1 = new Event(
                        "evento123",
                        "Cloud Native Summit Santiago 2026",
                        "Conferencia anual de arquitecturas en la nube, microservicios, AWS S3 y EFS.",
                        "Duoc UC - Auditorio Antonio Varas",
                        LocalDateTime.now().plusDays(15).withHour(18).withMinute(30),
                        new BigDecimal("25000.00"),
                        250
                );
                eventRepository.save(event1);
                log.info("Evento inicial creado: evento123 - Cloud Native Summit Santiago 2026");
            }

            if (!eventRepository.existsByEventCode("evento456")) {
                Event event2 = new Event(
                        "evento456",
                        "AWS & DevOps Engineering Bootcamp",
                        "Taller práctico intensivo de CI/CD con GitHub Actions, Docker y despliegue en EC2.",
                        "Duoc UC - Laboratorio Cloud 4",
                        LocalDateTime.now().plusDays(30).withHour(9).withMinute(0),
                        new BigDecimal("18500.00"),
                        40
                );
                eventRepository.save(event2);
                log.info("Evento inicial creado: evento456 - AWS & DevOps Engineering Bootcamp");
            }
        };
    }
}
