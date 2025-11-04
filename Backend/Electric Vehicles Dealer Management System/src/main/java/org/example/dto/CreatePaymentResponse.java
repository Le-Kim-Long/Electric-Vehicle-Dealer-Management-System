package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response object for created payment")
public class CreatePaymentResponse {

    @Schema(description = "Payment ID", example = "1")
    private Integer paymentId;

    @Schema(description = "Order ID", example = "1")
    private Integer orderId;

    @Schema(description = "Payment amount", example = "500000000.00")
    private BigDecimal amount;

    @Schema(description = "Payment date", example = "2024-11-02T10:30:00")
    private LocalDateTime paymentDate;

    @Schema(description = "Payment method", example = "Bank Transfer")
    private String method;

    @Schema(description = "Payment status", example = "Chờ xử lý")
    private String status;

    @Schema(description = "Payment note", example = "First installment payment")
    private String note;

    @Schema(description = "Success message", example = "Payment created successfully")
    private String message;
}
