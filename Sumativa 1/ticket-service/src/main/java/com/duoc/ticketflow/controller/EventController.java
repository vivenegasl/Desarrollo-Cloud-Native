package com.duoc.ticketflow.controller;

import com.duoc.ticketflow.dto.ApiMessageResponse;
import com.duoc.ticketflow.dto.EventRequest;
import com.duoc.ticketflow.dto.EventResponse;
import com.duoc.ticketflow.dto.EventStatsResponse;
import com.duoc.ticketflow.exception.ResourceNotFoundException;
import com.duoc.ticketflow.model.Event;
import com.duoc.ticketflow.repository.EventRepository;
import com.duoc.ticketflow.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Eventos y Estadísticas", description = "Endpoints para registro de eventos y reporte de estadísticas de ventas y reservas")
public class EventController {

    private final EventRepository eventRepository;
    private final TicketService ticketService;

    public EventController(EventRepository eventRepository, TicketService ticketService) {
        this.eventRepository = eventRepository;
        this.ticketService = ticketService;
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo evento", description = "Crea un evento en el sistema para permitir la emisión de tickets.")
    @ApiResponse(responseCode = "201", description = "Evento creado exitosamente")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        if (eventRepository.existsByEventCode(request.getEventCode())) {
            throw new IllegalArgumentException("Ya existe un evento registrado con el código: " + request.getEventCode());
        }

        Event event = new Event(
                request.getEventCode(),
                request.getName(),
                request.getDescription(),
                request.getVenue(),
                request.getEventDate(),
                request.getBasePrice(),
                request.getTotalCapacity()
        );

        Event saved = eventRepository.save(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.fromEntity(saved));
    }

    @GetMapping
    @Operation(summary = "Listar todos los eventos", description = "Devuelve el catálogo de eventos disponibles.")
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<EventResponse> events = eventRepository.findAll().stream()
                .map(EventResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventCode}")
    @Operation(summary = "Obtener detalles del evento", description = "Consulta la información de un evento por su código identificador.")
    public ResponseEntity<EventResponse> getEventByCode(@PathVariable String eventCode) {
        Event event = eventRepository.findByEventCode(eventCode)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con código: " + eventCode));
        return ResponseEntity.ok(EventResponse.fromEntity(event));
    }

    @GetMapping("/{eventCode}/stats")
    @Operation(summary = "7. Reportar estadísticas de ventas y reservas por evento", description = "Genera métricas consolidadas del evento: total tickets emitidos, activos, modificados, cancelados, recaudación total, tasa de ocupación y volumen de almacenamiento en AWS S3.")
    public ResponseEntity<EventStatsResponse> getEventStats(@PathVariable String eventCode) {
        EventStatsResponse stats = ticketService.getEventStats(eventCode);
        return ResponseEntity.ok(stats);
    }
}
