package com.duoc.ticketflow.dto;

import com.duoc.ticketflow.model.TicketCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Solicitud para generar un nuevo ticket personalizado")
public class CreateTicketRequest {

    @NotBlank(message = "El código del evento es obligatorio")
    @Schema(example = "evento123", description = "Identificador o código del evento")
    private String eventCode;

    @NotBlank(message = "El ID del usuario es obligatorio")
    @Schema(example = "usuarioX", description = "Identificador del usuario que realiza la reserva")
    private String userId;

    @NotBlank(message = "El nombre del asistente es obligatorio")
    @Schema(example = "Victor Lind", description = "Nombre completo del asistente")
    private String attendeeName;

    @NotBlank(message = "El email del asistente es obligatorio")
    @Email(message = "Formato de email inválido")
    @Schema(example = "victor.lind@duocuc.cl", description = "Correo electrónico del asistente")
    private String attendeeEmail;

    @NotBlank(message = "El número o código de asiento es obligatorio")
    @Schema(example = "A-12", description = "Asiento o zona asignada")
    private String seatNumber;

    @NotNull(message = "La categoría del ticket es obligatoria")
    @Schema(example = "VIP", description = "Categoría: GENERAL, VIP, PLATINUM, STUDENT")
    private TicketCategory category;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser un valor positivo")
    @Schema(example = "35000.00", description = "Precio final pagado por el ticket")
    private BigDecimal price;

    public CreateTicketRequest() {
    }

    public CreateTicketRequest(String eventCode, String userId, String attendeeName,
                               String attendeeEmail, String seatNumber, TicketCategory category, BigDecimal price) {
        this.eventCode = eventCode;
        this.userId = userId;
        this.attendeeName = attendeeName;
        this.attendeeEmail = attendeeEmail;
        this.seatNumber = seatNumber;
        this.category = category;
        this.price = price;
    }

    // Getters and Setters
    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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
}
