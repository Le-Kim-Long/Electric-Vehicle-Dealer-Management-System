package org.example.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateOrderPromotionResponse {
    private Integer orderId;
    private Integer promotionId;
    private String promotionName;
    private String promotionType;
    private BigDecimal promotionValue;
    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String message;
}
