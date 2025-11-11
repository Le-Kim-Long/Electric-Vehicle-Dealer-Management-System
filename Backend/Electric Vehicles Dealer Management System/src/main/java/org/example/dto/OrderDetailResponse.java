package org.example.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailResponse {
    private Integer orderDetailId;
    private Integer orderId;
    private Integer carId;
    private String carName;
    private String modelName;
    private String variantName;
    private String colorName;
    private Integer quantity;
    private Double unitPrice;
    private BigDecimal finalPrice;
}

