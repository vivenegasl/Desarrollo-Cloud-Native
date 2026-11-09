package com.duoc.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Solicitud para registrar un nuevo evento")
public class EventRequest {

    @NotBlank(message = "El código del evento es obligatorio")
    @Schema(example = "evento123", description = "Código único del evento")
    private String eventCode;

    @NotBlank(message = "El nombre del evento es obligatorio")
    @Schema(example = "Cloud Native Summit Santiago 2026", description = "Nombre oficial del evento")
    private String name;

    @Schema(example = "Conferencia sobre microservicios, AWS EFS, S3 y CI/CD", description = "Descripción detallada")
    private String description;

    @NotBlank(message = "El recinto es obligatorio")
    @Schema(example = "Duoc UC - Auditorio Antonio Varas", description = "Lugar o recinto del evento")
    private String venue;

    @NotNull(message = "La fecha del evento es obligatoria")
    @Schema(example = "2026-11-20T18:00:00", description = "Fecha y hora del evento")
    private LocalDateTime eventDate;

    @NotNull(message = "El precio base es obligatorio")
    @Positive(message = "El precio base debe ser mayor a 0")
    @Schema(example = "25000.00", description = "Precio base para entradas generales")
    private BigDecimal basePrice;

    @NotNull(message = "La capacidad total es obligatoria")
    @Positive(message = "La capacidad debe ser mayor a 0")
    @Schema(example = "500", description = "Capacidad máxima de asistentes")
    private Integer totalCapacity;

    public EventRequest() {
    }

    public EventRequest(String eventCode, String name, String description, String venue,
                        LocalDateTime eventDate, BigDecimal basePrice, Integer totalCapacity) {
        this.eventCode = eventCode;
        this.name = name;
        this.description = description;
        this.venue = venue;
        this.eventDate = eventDate;
        this.basePrice = basePrice;
        this.totalCapacity = totalCapacity;
    }

    // Getters and Setters
    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public Integer getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(Integer totalCapacity) {
        this.totalCapacity = totalCapacity;
    }
}
