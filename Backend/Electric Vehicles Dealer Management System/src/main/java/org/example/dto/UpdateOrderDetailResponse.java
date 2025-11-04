package org.example.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateOrderDetailResponse {
    private Integer orderDetailId;
    private Integer orderId;
    private Integer carId;
    private String modelName;
    private String variantName;
    private String colorName;
    private Integer oldQuantity;
    private Integer newQuantity;
    private BigDecimal unitPrice;
    private BigDecimal finalPrice;
    private String message;
}
