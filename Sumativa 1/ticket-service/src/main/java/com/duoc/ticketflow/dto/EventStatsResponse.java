package com.duoc.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Estadísticas completas de ventas, reservas y almacenamiento por evento")
public class EventStatsResponse {

    private String eventCode;
    private String eventName;
    private String venue;
    private LocalDateTime eventDate;
    private Integer totalCapacity;
    private Integer availableCapacity;
    private Double occupancyPercentage;
    private Long totalTicketsIssued;
    private Long activeTickets;
    private Long modifiedTickets;
    private Long cancelledTickets;
    private BigDecimal totalRevenue;
    private Long s3StoredFilesCount;
    private Long s3TotalBytes;
    private Double s3TotalKilobytes;
    private LocalDateTime generatedAt;

    public EventStatsResponse() {
    }

    // Getters and Setters
    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
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

    public Double getOccupancyPercentage() {
        return occupancyPercentage;
    }

    public void setOccupancyPercentage(Double occupancyPercentage) {
        this.occupancyPercentage = occupancyPercentage;
    }

    public Long getTotalTicketsIssued() {
        return totalTicketsIssued;
    }

    public void setTotalTicketsIssued(Long totalTicketsIssued) {
        this.totalTicketsIssued = totalTicketsIssued;
    }

    public Long getActiveTickets() {
        return activeTickets;
    }

    public void setActiveTickets(Long activeTickets) {
        this.activeTickets = activeTickets;
    }

    public Long getModifiedTickets() {
        return modifiedTickets;
    }

    public void setModifiedTickets(Long modifiedTickets) {
        this.modifiedTickets = modifiedTickets;
    }

    public Long getCancelledTickets() {
        return cancelledTickets;
    }

    public void setCancelledTickets(Long cancelledTickets) {
        this.cancelledTickets = cancelledTickets;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getS3StoredFilesCount() {
        return s3StoredFilesCount;
    }

    public void setS3StoredFilesCount(Long s3StoredFilesCount) {
        this.s3StoredFilesCount = s3StoredFilesCount;
    }

    public Long getS3TotalBytes() {
        return s3TotalBytes;
    }

    public void setS3TotalBytes(Long s3TotalBytes) {
        this.s3TotalBytes = s3TotalBytes;
    }

    public Double getS3TotalKilobytes() {
        return s3TotalKilobytes;
    }

    public void setS3TotalKilobytes(Double s3TotalKilobytes) {
        this.s3TotalKilobytes = s3TotalKilobytes;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
