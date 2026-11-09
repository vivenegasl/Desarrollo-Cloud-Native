package com.duoc.ticketflow.dto;

import com.duoc.ticketflow.model.TicketCategory;
import com.duoc.ticketflow.model.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Solicitud para modificar datos de un ticket existente")
public class UpdateTicketRequest {

    @Schema(example = "Victor Lind Actualizado", description = "Nombre actualizado del asistente")
    private String attendeeName;

    @Email(message = "Formato de email inválido")
    @Schema(example = "victor.updated@duocuc.cl", description = "Email actualizado del asistente")
    private String attendeeEmail;

    @Schema(example = "VIP-01", description = "Nuevo asiento reasignado")
    private String seatNumber;

    @Schema(example = "PLATINUM", description = "Nueva categoría")
    private TicketCategory category;

    @Positive(message = "El precio debe ser un valor positivo")
    @Schema(example = "50000.00", description = "Nuevo precio del ticket")
    private BigDecimal price;

    @Schema(example = "MODIFIED", description = "Nuevo estado del ticket")
    private TicketStatus status;

    public UpdateTicketRequest() {
    }

    // Getters and Setters
    public String getAttendeeName() {
        return attendeeName;
    }

    public void setAttendeeName(String attendeeName) {
        this.attendeeName = attendeeName;
    }

    public String getAttendeeEmail() {
        return attendeeEmail;
    }

    public void setAttendeeEmail(String attendeeEmail) {
        this.attendeeEmail = attendeeEmail;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }
}
