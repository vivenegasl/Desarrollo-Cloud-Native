package com.duoc.ticketflow.dto;

import com.duoc.ticketflow.model.Ticket;
import com.duoc.ticketflow.model.TicketCategory;
import com.duoc.ticketflow.model.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Detalle completo del ticket generado o consultado")
public class TicketResponse {

    private Long id;
    private String ticketCode;
    private String eventCode;
    private String userId;
    private String attendeeName;
    private String attendeeEmail;
    private String seatNumber;
    private TicketCategory category;
    private BigDecimal price;
    private TicketStatus status;

    @Schema(description = "Ruta física temporal en Amazon EFS montado")
    private String efsPath;

    @Schema(description = "Clave del objeto en Amazon S3 (/evento123/usuarioX/ticket001.pdf)")
    private String s3Key;

    private String s3Bucket;
    private Boolean s3Uploaded;
    private Long fileSizeBytes;
    private String checksumSha256;
    private String downloadUrl;
    private String presignedUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TicketResponse() {
    }

    public static TicketResponse fromEntity(Ticket ticket) {
        TicketResponse resp = new TicketResponse();
        resp.setId(ticket.getId());
        resp.setTicketCode(ticket.getTicketCode());
        resp.setEventCode(ticket.getEventCode());
        resp.setUserId(ticket.getUserId());
        resp.setAttendeeName(ticket.getAttendeeName());
        resp.setAttendeeEmail(ticket.getAttendeeEmail());
        resp.setSeatNumber(ticket.getSeatNumber());
        resp.setCategory(ticket.getCategory());
        resp.setPrice(ticket.getPrice());
        resp.setStatus(ticket.getStatus());
        resp.setEfsPath(ticket.getEfsPath());
        resp.setS3Key(ticket.getS3Key());
        resp.setS3Bucket(ticket.getS3Bucket());
        resp.setS3Uploaded(ticket.getS3Uploaded());
        resp.setFileSizeBytes(ticket.getFileSizeBytes());
        resp.setChecksumSha256(ticket.getChecksumSha256());
        resp.setDownloadUrl("/api/v1/tickets/" + ticket.getId() + "/download");
        resp.setCreatedAt(ticket.getCreatedAt());
        resp.setUpdatedAt(ticket.getUpdatedAt());
        return resp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

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

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getEfsPath() {
        return efsPath;
    }

    public void setEfsPath(String efsPath) {
        this.efsPath = efsPath;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    public Boolean getS3Uploaded() {
        return s3Uploaded;
    }

    public void setS3Uploaded(Boolean s3Uploaded) {
        this.s3Uploaded = s3Uploaded;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public void setChecksumSha256(String checksumSha256) {
        this.checksumSha256 = checksumSha256;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getPresignedUrl() {
        return presignedUrl;
    }

    public void setPresignedUrl(String presignedUrl) {
        this.presignedUrl = presignedUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
