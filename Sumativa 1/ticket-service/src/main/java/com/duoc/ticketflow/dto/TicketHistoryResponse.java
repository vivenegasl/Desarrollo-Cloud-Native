package com.duoc.ticketflow.dto;

import com.duoc.ticketflow.model.HistoryAction;
import com.duoc.ticketflow.model.TicketHistory;

import java.time.LocalDateTime;

public class TicketHistoryResponse {

    private Long id;
    private String ticketCode;
    private String eventCode;
    private String userId;
    private HistoryAction action;
    private String s3Key;
    private String efsPath;
    private String details;
    private String performedBy;
    private LocalDateTime timestamp;

    public TicketHistoryResponse() {
    }

    public static TicketHistoryResponse fromEntity(TicketHistory history) {
        TicketHistoryResponse resp = new TicketHistoryResponse();
        resp.setId(history.getId());
        resp.setTicketCode(history.getTicketCode());
        resp.setEventCode(history.getEventCode());
        resp.setUserId(history.getUserId());
        resp.setAction(history.getAction());
        resp.setS3Key(history.getS3Key());
        resp.setEfsPath(history.getEfsPath());
        resp.setDetails(history.getDetails());
        resp.setPerformedBy(history.getPerformedBy());
        resp.setTimestamp(history.getTimestamp());
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
