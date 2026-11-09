package com.duoc.ticketflow.controller;

import com.duoc.ticketflow.dto.*;
import com.duoc.ticketflow.model.HistoryAction;
import com.duoc.ticketflow.model.TicketStatus;
import com.duoc.ticketflow.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Tickets", description = "Endpoints para generación en EFS, persistencia en S3, descarga, modificación y consulta de tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/generate")
    @Operation(summary = "1. Generar ticket", description = "Genera el archivo PDF con código QR en Amazon EFS temporal y lo sube automáticamente a Amazon S3.")
    @ApiResponse(responseCode = "201", description = "Ticket generado, almacenado en EFS y subido a S3 exitosamente")
    public ResponseEntity<TicketResponse> generateTicket(@Valid @RequestBody CreateTicketRequest request) {
        TicketResponse response = ticketService.generateTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/upload-s3")
    @Operation(summary = "2. Subir ticket a S3", description = "Endpoint para subir explícitamente o re-sincronizar el archivo PDF del ticket a AWS S3.")
    public ResponseEntity<TicketResponse> uploadToS3(@PathVariable Long id) {
        TicketResponse response = ticketService.uploadTicketToS3(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalles del ticket", description = "Obtiene la información y metadatos del ticket registrado.")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketResponseById(id));
    }

    @GetMapping
    @Operation(summary = "Listar tickets con filtros", description = "Consulta tickets filtrando por evento, usuario o estado.")
    public ResponseEntity<List<TicketResponse>> getTickets(
            @RequestParam(required = false) String eventCode,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) TicketStatus status) {
        return ResponseEntity.ok(ticketService.getTickets(eventCode, userId, status));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "3. Descargar ticket (PDF)", description = "Descarga directa del ticket PDF desde AWS S3 con cabeceras HTTP de streaming.")
    public ResponseEntity<byte[]> downloadTicket(@PathVariable Long id) {
        TicketResponse ticket = ticketService.getTicketResponseById(id);
        byte[] pdfBytes = ticketService.downloadTicketPdf(id);

        String filename = String.format("%s_%s.pdf", ticket.getEventCode(), ticket.getTicketCode());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header("X-Ticket-Code", ticket.getTicketCode())
                .header("X-S3-Key", ticket.getS3Key())
                .body(pdfBytes);
    }

    @GetMapping("/{id}/presigned-url")
    @Operation(summary = "Obtener URL prefirmada de S3", description = "Genera una URL temporal firmada criptográficamente para descarga directa y segura desde el bucket S3.")
    public ResponseEntity<ApiMessageResponse> getPresignedUrl(@PathVariable Long id) {
        String url = ticketService.getPresignedUrl(id);
        return ResponseEntity.ok(ApiMessageResponse.ok("Presigned URL generada con éxito (válida por 15 minutos)", url));
    }

    @GetMapping("/download/user/{userId}")
    @Operation(summary = "Descargar todos los tickets de un usuario (ZIP)", description = "Descarga empaquetada en formato ZIP con todos los tickets generados para un usuario específico.")
    public ResponseEntity<byte[]> downloadUserTicketsZip(@PathVariable String userId) {
        byte[] zipBytes = ticketService.downloadTicketsZipForUser(userId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tickets_usuario_" + userId + ".zip\"")
                .body(zipBytes);
    }

    @GetMapping("/download/event/{eventCode}")
    @Operation(summary = "Descargar todos los tickets de un evento (ZIP)", description = "Descarga empaquetada en formato ZIP con todos los tickets emitidos para un evento.")
    public ResponseEntity<byte[]> downloadEventTicketsZip(@PathVariable String eventCode) {
        byte[] zipBytes = ticketService.downloadTicketsZipForEvent(eventCode);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tickets_evento_" + eventCode + ".zip\"")
                .body(zipBytes);
    }

    @PutMapping("/{id}")
    @Operation(summary = "4. Modificar detalles del ticket", description = "Actualiza información del ticket, regenera el PDF y actualiza el archivo en Amazon S3 y EFS.")
    public ResponseEntity<TicketResponse> updateTicket(@PathVariable Long id, @RequestBody UpdateTicketRequest request) {
        TicketResponse response = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "5. Eliminar ticket específico", description = "Elimina el ticket del sistema y purga los archivos correspondientes en Amazon S3 y Amazon EFS.")
    public ResponseEntity<ApiMessageResponse> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.ok(ApiMessageResponse.ok("Ticket eliminado correctamente de S3, EFS y Base de Datos."));
    }

    @GetMapping("/history")
    @Operation(summary = "6. Consultar historial de archivos generados", description = "Criterio 5: Consulta precisa y eficiente del historial de operaciones sobre archivos con filtros requeridos (fecha, usuario, evento, acción).")
    public ResponseEntity<Page<TicketHistoryResponse>> getFileHistory(
            @Parameter(description = "Código del evento") @RequestParam(required = false) String eventCode,
            @Parameter(description = "ID del usuario") @RequestParam(required = false) String userId,
            @Parameter(description = "Acción realizada (CREATED, S3_UPLOADED, UPDATED, DOWNLOADED, DELETED)") @RequestParam(required = false) HistoryAction action,
            @Parameter(description = "Fecha inicial (ISO-8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Fecha final (ISO-8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return ResponseEntity.ok(ticketService.getFileHistory(eventCode, userId, action, startDate, endDate, pageRequest));
    }
}
