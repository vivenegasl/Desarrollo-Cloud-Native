package com.duoc.ticketflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Respuesta estándar para confirmación de operaciones")
public class ApiMessageResponse {

    private boolean success;
    private String message;
    private Object data;
    private LocalDateTime timestamp;

    public ApiMessageResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiMessageResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ApiMessageResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static ApiMessageResponse ok(String message) {
        return new ApiMessageResponse(true, message);
    }

    public static ApiMessageResponse ok(String message, Object data) {
        return new ApiMessageResponse(true, message, data);
    }

    public static ApiMessageResponse error(String message) {
        return new ApiMessageResponse(false, message);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
