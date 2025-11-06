package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusResponse {
    private Integer orderId;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String customerName;
    private String dealerName;
    private String message;
}
