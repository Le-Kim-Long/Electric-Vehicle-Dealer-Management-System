package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdatePaymentStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(Chờ xử lý|Hoàn thành|Thất bại)$",
             message = "Status must be one of: Chờ xử lý, Hoàn thành, Thất bại")
    private String status;

    private String note;

    // Constructors
    public UpdatePaymentStatusRequest() {}

    public UpdatePaymentStatusRequest(String status, String note) {
        this.status = status;
        this.note = note;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
