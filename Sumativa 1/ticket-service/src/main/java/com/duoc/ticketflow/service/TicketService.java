package com.duoc.ticketflow.service;

import com.duoc.ticketflow.dto.CreateTicketRequest;
import com.duoc.ticketflow.dto.EventStatsResponse;
import com.duoc.ticketflow.dto.TicketHistoryResponse;
import com.duoc.ticketflow.dto.TicketResponse;
import com.duoc.ticketflow.dto.UpdateTicketRequest;
import com.duoc.ticketflow.exception.ResourceNotFoundException;
import com.duoc.ticketflow.exception.StorageException;
import com.duoc.ticketflow.model.*;
import com.duoc.ticketflow.repository.EventRepository;
import com.duoc.ticketflow.repository.TicketHistoryRepository;
import com.duoc.ticketflow.repository.TicketRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);
    private static final DateTimeFormatter EVENT_DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final TicketHistoryRepository historyRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final EfsStorageService efsStorageService;
    private final S3StorageService s3StorageService;

    public TicketService(TicketRepository ticketRepository,
                         EventRepository eventRepository,
                         TicketHistoryRepository historyRepository,
                         PdfGeneratorService pdfGeneratorService,
                         EfsStorageService efsStorageService,
                         S3StorageService s3StorageService) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
        this.historyRepository = historyRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.efsStorageService = efsStorageService;
        this.s3StorageService = s3StorageService;
    }

    /**
     * Requisito 1, 2 y 3: Genera ticket, almacena temporalmente en EFS y sube automáticamente a S3
     */
    @Transactional
    public TicketResponse generateTicket(CreateTicketRequest request) {
        Event event = eventRepository.findByEventCode(request.getEventCode())
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con código: " + request.getEventCode()));

        if (event.getAvailableCapacity() <= 0) {
            throw new IllegalArgumentException("El evento '" + event.getName() + "' no cuenta con cupos disponibles.");
        }

        // Generar código de ticket único (ej. TCK-8A19F3 o ticket001)
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String ticketCode = "TCK-" + uniqueSuffix;

        // Estructura requerida: /{eventoId}/{usuarioId}/{ticketId}.pdf
        String s3Key = String.format("%s/%s/%s.pdf", request.getEventCode(), request.getUserId(), ticketCode);
        String efsCanonicalPath = efsStorageService.getTicketCanonicalPath(request.getEventCode(), request.getUserId(), ticketCode);

        // Instanciar Entidad Ticket
        Ticket ticket = new Ticket(
                ticketCode,
                request.getEventCode(),
                request.getUserId(),
                request.getAttendeeName(),
                request.getAttendeeEmail(),
                request.getSeatNumber(),
                request.getCategory(),
                request.getPrice() != null ? request.getPrice() : event.getBasePrice(),
                efsCanonicalPath,
                s3Key,
                s3StorageService.getBucketName()
        );

        // 1. Generar contenido PDF con QR
        String formattedDate = event.getEventDate() != null ? event.getEventDate().format(EVENT_DATE_FMT) : "Por definir";
        byte[] pdfBytes = pdfGeneratorService.generateTicketPdf(ticket, event.getName(), event.getVenue(), formattedDate);

        // Calcular Checksum SHA-256 para verificar integridad
        String checksum = calculateSha256(pdfBytes);
        ticket.setChecksumSha256(checksum);
        ticket.setFileSizeBytes((long) pdfBytes.length);

        // 2. Almacenamiento Temporal en Amazon EFS (Criterio 1)
        Path efsSavedPath = efsStorageService.saveTicketToEfs(request.getEventCode(), request.getUserId(), ticketCode, pdfBytes);
        ticket.setEfsPath(efsSavedPath.toString());

        // 3. Subida Automática a Amazon S3 (Criterio 2)
        Map<String, String> s3Metadata = new HashMap<>();
        s3Metadata.put("event-code", request.getEventCode());
        s3Metadata.put("user-id", request.getUserId());
        s3Metadata.put("attendee-name", request.getAttendeeName());
        s3Metadata.put("checksum-sha256", checksum);

        s3StorageService.uploadFile(s3Key, pdfBytes, "application/pdf", s3Metadata);
        ticket.setS3Uploaded(true);

        // Actualizar capacidad del evento
        event.setAvailableCapacity(event.getAvailableCapacity() - 1);
        eventRepository.save(event);

        // Guardar ticket en Base de Datos
        Ticket savedTicket = ticketRepository.save(ticket);

        // Registrar auditoría en historial (Criterio 5)
        recordHistory(ticketCode, request.getEventCode(), request.getUserId(), HistoryAction.CREATED,
                s3Key, ticket.getEfsPath(), "Ticket generado y almacenado en EFS temporal", request.getUserId());
        recordHistory(ticketCode, request.getEventCode(), request.getUserId(), HistoryAction.S3_UPLOADED,
                s3Key, ticket.getEfsPath(), "Subida automática a S3 completada: s3://" + s3StorageService.getBucketName() + "/" + s3Key, "SYSTEM");

        TicketResponse response = TicketResponse.fromEntity(savedTicket);
        response.setPresignedUrl(s3StorageService.generatePresignedDownloadUrl(s3Key, 15));
        return response;
    }

    /**
     * Endpoint explícito para subir/sincronizar ticket a S3
     */
    @Transactional
    public TicketResponse uploadTicketToS3(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con ID: " + ticketId));

        byte[] pdfBytes;
        if (efsStorageService.existsInEfs(ticket.getEventCode(), ticket.getUserId(), ticket.getTicketCode())) {
            pdfBytes = efsStorageService.readTicketFromEfs(ticket.getEventCode(), ticket.getUserId(), ticket.getTicketCode());
        } else {
            Event event = eventRepository.findByEventCode(ticket.getEventCode()).orElse(null);
            String eventName = event != null ? event.getName() : "Evento";
            String venue = event != null ? event.getVenue() : "Recinto";
            String dateStr = event != null && event.getEventDate() != null ? event.getEventDate().format(EVENT_DATE_FMT) : "";
            pdfBytes = pdfGeneratorService.generateTicketPdf(ticket, eventName, venue, dateStr);
            efsStorageService.saveTicketToEfs(ticket.getEventCode(), ticket.getUserId(), ticket.getTicketCode(), pdfBytes);
        }

        Map<String, String> metadata = new HashMap<>();
        metadata.put("event-code", ticket.getEventCode());
        metadata.put("user-id", ticket.getUserId());
        metadata.put("checksum-sha256", ticket.getChecksumSha256() != null ? ticket.getChecksumSha256() : "");

        s3StorageService.uploadFile(ticket.getS3Key(), pdfBytes, "application/pdf", metadata);
        ticket.setS3Uploaded(true);
        ticketRepository.save(ticket);

        recordHistory(ticket.getTicketCode(), ticket.getEventCode(), ticket.getUserId(),
                HistoryAction.S3_UPLOADED, ticket.getS3Key(), ticket.getEfsPath(),
                "Subida manual/re-sincronización a AWS S3 ejecutada", "ADMIN");

        TicketResponse resp = TicketResponse.fromEntity(ticket);
        resp.setPresignedUrl(s3StorageService.generatePresignedDownloadUrl(ticket.getS3Key(), 15));
        return resp;
    }

    /**
     * Criterio 3: Modifica y actualiza los archivos en AWS S3
     */
    @Transactional
    public TicketResponse updateTicket(Long ticketId, UpdateTicketRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con ID: " + ticketId));

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalArgumentException("No se puede modificar un ticket que ha sido cancelado.");
        }

        // Aplicar cambios en campos modificables
        if (StringUtils.hasText(request.getAttendeeName())) {
            ticket.setAttendeeName(request.getAttendeeName());
        }
        if (StringUtils.hasText(request.getAttendeeEmail())) {
            ticket.setAttendeeEmail(request.getAttendeeEmail());
        }
        if (StringUtils.hasText(request.getSeatNumber())) {
            ticket.setSeatNumber(request.getSeatNumber());
        }
        if (request.getCategory() != null) {
            ticket.setCategory(request.getCategory());
        }
        if (request.getPrice() != null) {
            ticket.setPrice(request.getPrice());
        }
        ticket.setStatus(TicketStatus.MODIFIED);

        // Regenerar el PDF con los datos actualizados
        Event event = eventRepository.findByEventCode(ticket.getEventCode()).orElse(null);
        String eventName = event != null ? event.getName() : "Evento";
        String venue = event != null ? event.getVenue() : "Recinto";
        String dateStr = event != null && event.getEventDate() != null ? event.getEventDate().format(EVENT_DATE_FMT) : "";

        byte[] updatedPdfBytes = pdfGeneratorService.generateTicketPdf(ticket, eventName, venue, dateStr);
        String newChecksum = calculateSha256(updatedPdfBytes);
        ticket.setChecksumSha256(newChecksum);
        ticket.setFileSizeBytes((long) updatedPdfBytes.length);

        // 1. Actualizar copia en EFS
        efsStorageService.saveTicketToEfs(ticket.getEventCode(), ticket.getUserId(), ticket.getTicketCode(), updatedPdfBytes);

        // 2. Modificar y actualizar archivo en AWS S3 (Criterio 3)
        Map<String, String> updateMetadata = new HashMap<>();
        updateMetadata.put("event-code", ticket.getEventCode());
        updateMetadata.put("user-id", ticket.getUserId());
        updateMetadata.put("attendee-name", ticket.getAttendeeName());
        updateMetadata.put("checksum-sha256", newChecksum);
        updateMetadata.put("modification-timestamp", LocalDateTime.now().toString());

        s3StorageService.updateFile(ticket.getS3Key(), updatedPdfBytes, "application/pdf", updateMetadata);

        Ticket savedTicket = ticketRepository.save(ticket);

        recordHistory(ticket.getTicketCode(), ticket.getEventCode(), ticket.getUserId(),
                HistoryAction.UPDATED, ticket.getS3Key(), ticket.getEfsPath(),
                "Ticket modificado. PDF regenerado y actualizado en EFS y AWS S3", ticket.getUserId());

        TicketResponse resp = TicketResponse.fromEntity(savedTicket);
        resp.setPresignedUrl(s3StorageService.generatePresignedDownloadUrl(ticket.getS3Key(), 15));
        return resp;
    }

    /**
     * Criterio 4: Descarga los archivos desde AWS S3
     */
    public byte[] downloadTicketPdf(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con ID: " + ticketId));

        byte[] pdfBytes;
        // Intenta descargar prioritariamente de AWS S3
        try {
            pdfBytes = s3StorageService.downloadFile(ticket.getS3Key());
            log.info("Ticket descargado exitosamente desde AWS S3: {}", ticket.getS3Key());
        } catch (Exception e) {
            log.warn("Fallo descarga desde S3 [{}]. Intentando recuperación desde EFS temporal: {}", ticket.getS3Key(), e.getMessage());
            pdfBytes = efsStorageService.readTicketFromEfs(ticket.getEventCode(), ticket.getUserId(), ticket.getTicketCode());
        }

        recordHistory(ticket.getTicketCode(), ticket.getEventCode(), ticket.getUserId(),
                HistoryAction.DOWNLOADED, ticket.getS3Key(), ticket.getEfsPath(),
                "Descarga de ticket efectuada desde AWS S3", ticket.getUserId());

        return pdfBytes;
    }

    /**
     * Genera URL prefirmada (Pre-signed URL) de S3
     */
    public String getPresignedUrl(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con ID: " + ticketId));

        String presignedUrl = s3StorageService.generatePresignedDownloadUrl(ticket.getS3Key(), 15);
        if (presignedUrl == null) {
            throw new StorageException("No se pudo generar la URL prefirmada para el ticket: " + ticket.getTicketCode());
        }
        return presignedUrl;
    }

    /**
     * Elimina un ticket específico de S3, EFS y actualiza base de datos
     */
    @Transactional
    public void deleteTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con ID: " + ticketId));

        // 1. Eliminar archivo en S3
        s3StorageService.deleteFile(ticket.getS3Key());

        // 2. Eliminar archivo en EFS
        efsStorageService.deleteTicketFromEfs(ticket.getEventCode(), ticket.getUserId(), ticket.getTicketCode());

        // 3. Devolver capacidad al evento
        eventRepository.findByEventCode(ticket.getEventCode()).ifPresent(event -> {
            event.setAvailableCapacity(Math.min(event.getTotalCapacity(), event.getAvailableCapacity() + 1));
            eventRepository.save(event);
        });

        // 4. Marcar como CANCELLED y registrar en historial
        ticket.setStatus(TicketStatus.CANCELLED);
        ticketRepository.save(ticket);

        recordHistory(ticket.getTicketCode(), ticket.getEventCode(), ticket.getUserId(),
                HistoryAction.DELETED, ticket.getS3Key(), ticket.getEfsPath(),
                "Ticket cancelado y archivos purgados de S3 y EFS", "ADMIN");
    }

    /**
     * Criterio 5: Consulta el historial de archivos generados con filtros requeridos
     */
    public Page<TicketHistoryResponse> getFileHistory(String eventCode, String userId, HistoryAction action,
                                                      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Specification<TicketHistory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(eventCode)) {
                predicates.add(cb.equal(root.get("eventCode"), eventCode));
            }
            if (StringUtils.hasText(userId)) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (action != null) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), endDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return historyRepository.findAll(spec, pageable).map(TicketHistoryResponse::fromEntity);
    }

    /**
     * Consulta tickets con filtros
     */
    public List<TicketResponse> getTickets(String eventCode, String userId, TicketStatus status) {
        Specification<Ticket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(eventCode)) {
                predicates.add(cb.equal(root.get("eventCode"), eventCode));
            }
            if (StringUtils.hasText(userId)) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return ticketRepository.findAll(spec).stream()
                .map(t -> {
                    TicketResponse r = TicketResponse.fromEntity(t);
                    r.setPresignedUrl(s3StorageService.generatePresignedDownloadUrl(t.getS3Key(), 15));
                    return r;
                })
                .toList();
    }

    public TicketResponse getTicketResponseById(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con ID: " + ticketId));
        TicketResponse resp = TicketResponse.fromEntity(ticket);
        resp.setPresignedUrl(s3StorageService.generatePresignedDownloadUrl(ticket.getS3Key(), 15));
        return resp;
    }

    /**
     * Reporta estadísticas de ventas y reservas por evento
     */
    public EventStatsResponse getEventStats(String eventCode) {
        Event event = eventRepository.findByEventCode(eventCode)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con código: " + eventCode));

        long totalTickets = ticketRepository.countByEventCode(eventCode);
        long activeTickets = ticketRepository.countByEventCodeAndStatus(eventCode, TicketStatus.ACTIVE);
        long modifiedTickets = ticketRepository.countByEventCodeAndStatus(eventCode, TicketStatus.MODIFIED);
        long cancelledTickets = ticketRepository.countByEventCodeAndStatus(eventCode, TicketStatus.CANCELLED);
        BigDecimal totalRevenue = ticketRepository.sumRevenueByEventCode(eventCode);
        Long s3Bytes = ticketRepository.sumS3BytesByEventCode(eventCode);

        double occupancy = 0.0;
        if (event.getTotalCapacity() > 0) {
            occupancy = ((double) (totalTickets - cancelledTickets) / event.getTotalCapacity()) * 100.0;
        }

        EventStatsResponse stats = new EventStatsResponse();
        stats.setEventCode(event.getEventCode());
        stats.setEventName(event.getName());
        stats.setVenue(event.getVenue());
        stats.setEventDate(event.getEventDate());
        stats.setTotalCapacity(event.getTotalCapacity());
        stats.setAvailableCapacity(event.getAvailableCapacity());
        stats.setOccupancyPercentage(BigDecimal.valueOf(occupancy).setScale(2, RoundingMode.HALF_UP).doubleValue());
        stats.setTotalTicketsIssued(totalTickets);
        stats.setActiveTickets(activeTickets);
        stats.setModifiedTickets(modifiedTickets);
        stats.setCancelledTickets(cancelledTickets);
        stats.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        stats.setS3StoredFilesCount(activeTickets + modifiedTickets);
        stats.setS3TotalBytes(s3Bytes != null ? s3Bytes : 0L);
        stats.setS3TotalKilobytes(s3Bytes != null ? BigDecimal.valueOf(s3Bytes / 1024.0).setScale(2, RoundingMode.HALF_UP).doubleValue() : 0.0);
        stats.setGeneratedAt(LocalDateTime.now());

        return stats;
    }

    /**
     * Descarga empaquetada (ZIP) de tickets por usuario
     */
    public byte[] downloadTicketsZipForUser(String userId) {
        List<Ticket> tickets = ticketRepository.findByUserId(userId).stream()
                .filter(t -> t.getStatus() != TicketStatus.CANCELLED)
                .toList();

        if (tickets.isEmpty()) {
            throw new ResourceNotFoundException("No existen tickets activos para el usuario: " + userId);
        }

        return createZipPackage(tickets);
    }

    /**
     * Descarga empaquetada (ZIP) de tickets por evento
     */
    public byte[] downloadTicketsZipForEvent(String eventCode) {
        List<Ticket> tickets = ticketRepository.findByEventCode(eventCode).stream()
                .filter(t -> t.getStatus() != TicketStatus.CANCELLED)
                .toList();

        if (tickets.isEmpty()) {
            throw new ResourceNotFoundException("No existen tickets activos para el evento: " + eventCode);
        }

        return createZipPackage(tickets);
    }

    private byte[] createZipPackage(List<Ticket> tickets) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (Ticket ticket : tickets) {
                byte[] pdfBytes;
                try {
                    pdfBytes = s3StorageService.downloadFile(ticket.getS3Key());
                } catch (Exception ex) {
                    pdfBytes = efsStorageService.readTicketFromEfs(ticket.getEventCode(), ticket.getUserId(), ticket.getTicketCode());
                }

                String filename = String.format("%s_%s_%s.pdf", ticket.getEventCode(), ticket.getUserId(), ticket.getTicketCode());
                ZipEntry entry = new ZipEntry(filename);
                zos.putNextEntry(entry);
                zos.write(pdfBytes);
                zos.closeEntry();
            }
            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new StorageException("Error generando archivo ZIP comprimido de tickets", e);
        }
    }

    private void recordHistory(String ticketCode, String eventCode, String userId, HistoryAction action,
                               String s3Key, String efsPath, String details, String performedBy) {
        TicketHistory history = new TicketHistory(ticketCode, eventCode, userId, action, s3Key, efsPath, details, performedBy);
        historyRepository.save(history);
    }

    private String calculateSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }
}
