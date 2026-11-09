package com.duoc.ticketflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_history", indexes = {
    @Index(name = "idx_history_ticket", columnList = "ticketCode"),
    @Index(name = "idx_history_event", columnList = "eventCode"),
    @Index(name = "idx_history_user", columnList = "userId"),
    @Index(name = "idx_history_action", columnList = "action"),
    @Index(name = "idx_history_timestamp", columnList = "timestamp")
})
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String ticketCode;

    @Column(nullable = false, length = 64)
    private String eventCode;

    @Column(nullable = false, length = 64)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private HistoryAction action;

    @Column(length = 512)
    private String s3Key;

    @Column(length = 512)
    private String efsPath;

    @Column(length = 1000)
    private String details;

    @Column(nullable = false, length = 64)
    private String performedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public TicketHistory() {
    }

    public TicketHistory(String ticketCode, String eventCode, String userId, HistoryAction action,
                         String s3Key, String efsPath, String details, String performedBy) {
        this.ticketCode = ticketCode;
        this.eventCode = eventCode;
        this.userId = userId;
        this.action = action;
        this.s3Key = s3Key;
        this.efsPath = efsPath;
        this.details = details;
        this.performedBy = performedBy;
    }

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
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

    public HistoryAction getAction() {
        return action;
    }

    public void setAction(HistoryAction action) {
        this.action = action;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getEfsPath() {
        return efsPath;
    }

    public void setEfsPath(String efsPath) {
        this.efsPath = efsPath;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
