package com.duoc.ticketflow;

import com.duoc.ticketflow.model.Ticket;
import com.duoc.ticketflow.model.TicketCategory;
import com.duoc.ticketflow.model.TicketStatus;
import com.duoc.ticketflow.service.EfsStorageService;
import com.duoc.ticketflow.service.PdfGeneratorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class TicketFlowApplicationTests {

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private EfsStorageService efsStorageService;

    @Test
    @DisplayName("El contexto de Spring Boot carga correctamente")
    void contextLoads() {
        assertNotNull(pdfGeneratorService);
        assertNotNull(efsStorageService);
    }

    @Test
    @DisplayName("Generación de PDF de ticket con OpenPDF y ZXing QR Code")
    void testPdfGeneration() {
        Ticket ticket = new Ticket(
                "TCK-TEST01",
                "evento123",
                "usuarioX",
                "Victor Lind Test",
                "test@duocuc.cl",
                "VIP-01",
                TicketCategory.VIP,
                new BigDecimal("35000.00"),
                "/mnt/efs/tickets/evento123/usuarioX/TCK-TEST01.pdf",
                "evento123/usuarioX/TCK-TEST01.pdf",
                "test-bucket"
        );

        byte[] pdfBytes = pdfGeneratorService.generateTicketPdf(
                ticket,
                "Cloud Native Summit 2026",
                "Duoc UC - Auditorio Central",
                "20/11/2026 18:30"
        );

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500, "El PDF generado debe contener datos legibles");
        // Los primeros 4 bytes de un PDF siempre son %PDF
        String header = new String(pdfBytes, 0, 4);
        assertEquals("%PDF", header, "El archivo debe iniciar con la cabecera estándar de PDF");
    }

    @Test
    @DisplayName("Criterio 1: Almacenamiento temporal en EFS montado")
    void testEfsStorageLifecycle() {
        String eventCode = "evento123";
        String userId = "usuarioX";
        String ticketCode = "TCK-EFS-UNIT";
        byte[] sampleData = "Contenido de prueba para almacenamiento EFS".getBytes();

        // 1. Guardar en EFS
        Path savedPath = efsStorageService.saveTicketToEfs(eventCode, userId, ticketCode, sampleData);
        assertNotNull(savedPath);
        assertTrue(efsStorageService.existsInEfs(eventCode, userId, ticketCode));

        // 2. Leer desde EFS
        byte[] retrievedData = efsStorageService.readTicketFromEfs(eventCode, userId, ticketCode);
        assertArrayEquals(sampleData, retrievedData);

        // 3. Obtener tamaño
        long size = efsStorageService.getFileSize(eventCode, userId, ticketCode);
        assertEquals(sampleData.length, size);

        // 4. Eliminar de EFS
        boolean deleted = efsStorageService.deleteTicketFromEfs(eventCode, userId, ticketCode);
        assertTrue(deleted);
        assertFalse(efsStorageService.existsInEfs(eventCode, userId, ticketCode));
    }
}
