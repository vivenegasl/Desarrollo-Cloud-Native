package com.duoc.ticketflow.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets", indexes = {
    @Index(name = "idx_ticket_event", columnList = "eventCode"),
    @Index(name = "idx_ticket_user", columnList = "userId"),
    @Index(name = "idx_ticket_status", columnList = "status"),
    @Index(name = "idx_ticket_created", columnList = "createdAt")
})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String ticketCode;

    @Column(nullable = false, length = 64)
    private String eventCode;

    @Column(nullable = false, length = 64)
    private String userId;

    @Column(nullable = false, length = 120)
    private String attendeeName;

    @Column(nullable = false, length = 120)
    private String attendeeEmail;

    @Column(nullable = false, length = 32)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TicketCategory category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TicketStatus status;

    @Column(nullable = false, length = 512)
    private String efsPath;

    @Column(nullable = false, length = 512)
    private String s3Key;

    @Column(length = 128)
    private String s3Bucket;

    @Column(nullable = false)
    private Boolean s3Uploaded = false;

    private Long fileSizeBytes;

    @Column(length = 64)
    private String checksumSha256;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Ticket() {
    }

    public Ticket(String ticketCode, String eventCode, String userId, String attendeeName,
                  String attendeeEmail, String seatNumber, TicketCategory category,
                  BigDecimal price, String efsPath, String s3Key, String s3Bucket) {
        this.ticketCode = ticketCode;
        this.eventCode = eventCode;
        this.userId = userId;
        this.attendeeName = attendeeName;
        this.attendeeEmail = attendeeEmail;
        this.seatNumber = seatNumber;
        this.category = category;
        this.price = price;
        this.status = TicketStatus.ACTIVE;
        this.efsPath = efsPath;
        this.s3Key = s3Key;
        this.s3Bucket = s3Bucket;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = TicketStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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
