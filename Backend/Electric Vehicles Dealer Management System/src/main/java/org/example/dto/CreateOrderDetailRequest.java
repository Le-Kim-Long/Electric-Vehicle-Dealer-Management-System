package org.example.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderDetailRequest {
    private Integer orderId;
    private String modelName;
    private String variantName;
    private String colorName;
    private Integer quantity;
}
