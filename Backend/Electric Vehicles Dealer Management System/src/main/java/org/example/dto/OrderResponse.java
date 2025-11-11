package org.example.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private Integer orderId;
    private Integer customerId;
    private String customerName;
    private Integer dealerId;
    private String dealerName;
    private LocalDateTime orderDate;
    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String status;
    private Integer promotionId;
    private String promotionName;
}

