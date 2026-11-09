package com.duoc.ticketflow.dto;

import com.duoc.ticketflow.model.Event;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Detalles de un evento registrado")
public class EventResponse {

    private Long id;
    private String eventCode;
    private String name;
    private String description;
    private String venue;
    private LocalDateTime eventDate;
    private BigDecimal basePrice;
    private Integer totalCapacity;
    private Integer availableCapacity;
    private LocalDateTime createdAt;

    public EventResponse() {
    }

    public static EventResponse fromEntity(Event event) {
        EventResponse resp = new EventResponse();
        resp.setId(event.getId());
        resp.setEventCode(event.getEventCode());
        resp.setName(event.getName());
        resp.setDescription(event.getDescription());
        resp.setVenue(event.getVenue());
        resp.setEventDate(event.getEventDate());
        resp.setBasePrice(event.getBasePrice());
        resp.setTotalCapacity(event.getTotalCapacity());
        resp.setAvailableCapacity(event.getAvailableCapacity());
        resp.setCreatedAt(event.getCreatedAt());
        return resp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(Integer availableCapacity) {
        this.availableCapacity = availableCapacity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
