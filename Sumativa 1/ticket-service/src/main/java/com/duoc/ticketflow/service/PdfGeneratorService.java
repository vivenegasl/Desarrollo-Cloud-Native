package com.duoc.ticketflow.service;

import com.duoc.ticketflow.exception.StorageException;
import com.duoc.ticketflow.model.Ticket;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generateTicketPdf(Ticket ticket, String eventName, String venue, String eventDateStr) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A5.rotate(), 20, 20, 20, 20);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Estilos de fuentes
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.WHITE);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(220, 220, 220));
            Font headerLabelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(100, 110, 120));
            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(20, 30, 45));
            Font codeFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 14, new Color(0, 102, 204));
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

            // Tabla Principal tipo Tarjeta / Ticket
            PdfPTable mainCard = new PdfPTable(1);
            mainCard.setWidthPercentage(100);

            // 1. Cabecera Corporativa con gradiente / fondo oscuro
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);

            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(new Color(24, 32, 54));
            headerCell.setPadding(14);
            headerCell.setBorder(Rectangle.NO_BORDER);

            Paragraph pBadge = new Paragraph("TICKET OFICIAL DE ACCESO • DUOC UC CLOUD NATIVE", subtitleFont);
            pBadge.setAlignment(Element.ALIGN_CENTER);
            headerCell.addElement(pBadge);

            Paragraph pEventTitle = new Paragraph(eventName != null ? eventName.toUpperCase() : "EVENTO CLOUD NATIVE", titleFont);
            pEventTitle.setAlignment(Element.ALIGN_CENTER);
            pEventTitle.setSpacingBefore(4);
            headerCell.addElement(pEventTitle);

            Paragraph pVenueDate = new Paragraph("📍 " + (venue != null ? venue : "Recinto Oficial") + "   |   📅 " + (eventDateStr != null ? eventDateStr : "Por confirmar"), subtitleFont);
            pVenueDate.setAlignment(Element.ALIGN_CENTER);
            pVenueDate.setSpacingBefore(4);
            headerCell.addElement(pVenueDate);

            headerTable.addCell(headerCell);

            PdfPCell cardHeaderCell = new PdfPCell(headerTable);
            cardHeaderCell.setBorder(Rectangle.NO_BORDER);
            mainCard.addCell(cardHeaderCell);

            // 2. Cuerpo del Ticket (2 columnas: Datos Asistente a la izquierda, QR a la derecha)
            PdfPTable bodyTable = new PdfPTable(2);
            bodyTable.setWidthPercentage(100);
            bodyTable.setWidths(new float[]{68f, 32f});

            // Columna Izquierda: Datos del Ticket
            PdfPCell dataCell = new PdfPCell();
            dataCell.setPadding(14);
            dataCell.setBackgroundColor(new Color(250, 252, 255));
            dataCell.setBorder(Rectangle.NO_BORDER);

            PdfPTable grid = new PdfPTable(2);
            grid.setWidthPercentage(100);
            grid.setWidths(new float[]{50f, 50f});

            addGridItem(grid, "CÓDIGO DE TICKET", ticket.getTicketCode(), codeFont, headerLabelFont);
            addGridItem(grid, "CATEGORÍA", ticket.getCategory() != null ? ticket.getCategory().name() : "GENERAL", dataFont, headerLabelFont);
            addGridItem(grid, "ASISTENTE", ticket.getAttendeeName(), dataFont, headerLabelFont);
            addGridItem(grid, "ASIENTO / UBICACIÓN", ticket.getSeatNumber(), dataFont, headerLabelFont);
            addGridItem(grid, "EMAIL", ticket.getAttendeeEmail(), dataFont, headerLabelFont);
            addGridItem(grid, "PRECIO PAGADO", "$" + (ticket.getPrice() != null ? ticket.getPrice().toString() : "0.00"), dataFont, headerLabelFont);
            addGridItem(grid, "ESTADO", ticket.getStatus() != null ? ticket.getStatus().name() : "ACTIVE", dataFont, headerLabelFont);
            addGridItem(grid, "ID USUARIO", ticket.getUserId(), dataFont, headerLabelFont);

            dataCell.addElement(grid);
            bodyTable.addCell(dataCell);

            // Columna Derecha: Código QR con información de validación
            PdfPCell qrCell = new PdfPCell();
            qrCell.setPadding(10);
            qrCell.setBackgroundColor(new Color(245, 247, 250));
            qrCell.setBorder(Rectangle.LEFT);
            qrCell.setBorderColor(new Color(220, 225, 230));
            qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            qrCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            byte[] qrBytes = generateQrCodeImage("TICKET:" + ticket.getTicketCode() + "|EVENT:" + ticket.getEventCode() + "|USER:" + ticket.getUserId(), 140, 140);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.setAlignment(Element.ALIGN_CENTER);
            qrCell.addElement(qrImage);

            Paragraph qrLabel = new Paragraph("ESCANEE PARA ACCESO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.DARK_GRAY));
            qrLabel.setAlignment(Element.ALIGN_CENTER);
            qrLabel.setSpacingBefore(4);
            qrCell.addElement(qrLabel);

            bodyTable.addCell(qrCell);

            PdfPCell cardBodyCell = new PdfPCell(bodyTable);
            cardBodyCell.setBorder(Rectangle.NO_BORDER);
            mainCard.addCell(cardBodyCell);

            // 3. Pie de página de seguridad y almacenamiento Cloud
            PdfPCell footerCell = new PdfPCell();
            footerCell.setBackgroundColor(new Color(238, 242, 246));
            footerCell.setPadding(8);
            footerCell.setBorder(Rectangle.TOP);
            footerCell.setBorderColor(new Color(210, 215, 225));

            String cloudInfo = String.format("AWS Cloud Native Ticket • EFS Mount: %s • S3 Key: %s",
                    ticket.getEfsPath() != null ? ticket.getEfsPath() : "NFS/EFS",
                    ticket.getS3Key() != null ? ticket.getS3Key() : "S3");

            Paragraph pCloud = new Paragraph(cloudInfo, footerFont);
            pCloud.setAlignment(Element.ALIGN_CENTER);
            footerCell.addElement(pCloud);

            Paragraph pNotice = new Paragraph("Este ticket es intransferible y debe ser presentado en formato digital o impreso al ingresar al recinto.", footerFont);
            pNotice.setAlignment(Element.ALIGN_CENTER);
            footerCell.addElement(pNotice);

            mainCard.addCell(footerCell);

            document.add(mainCard);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new StorageException("Error generando PDF para ticket: " + ticket.getTicketCode(), e);
        }
    }

    private void addGridItem(PdfPTable table, String label, String value, Font valueFont, Font labelFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingBottom(6);

        Paragraph pLabel = new Paragraph(label, labelFont);
        Paragraph pValue = new Paragraph(value != null ? value : "-", valueFont);
        cell.addElement(pLabel);
        cell.addElement(pValue);

        table.addCell(cell);
    }

    private byte[] generateQrCodeImage(String text, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }
}
