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
@Schema(description = "Response object for updated payment method")
public class UpdatePaymentMethodResponse {

    @Schema(description = "Payment ID", example = "1")
    private Integer paymentId;

    @Schema(description = "Order ID", example = "1")
    private Integer orderId;

    @Schema(description = "Payment amount", example = "500000000.00")
    private BigDecimal amount;

    @Schema(description = "Payment date", example = "2024-11-02T10:30:00")
    private LocalDateTime paymentDate;

    @Schema(description = "Updated payment method", example = "Credit Card")
    private String method;

    @Schema(description = "Payment status (automatically set to 'Chờ xử lý')", example = "Chờ xử lý")
    private String status;

    @Schema(description = "Updated payment note", example = "Changed to credit card payment")
    private String note;

    @Schema(description = "Success message", example = "Payment method updated successfully")
    private String message;
}
