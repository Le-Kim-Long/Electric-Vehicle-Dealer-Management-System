package org.example.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateOrderPaymentMethodResponse {
    private Integer orderId;
    private String paymentMethod;
    private String status;
    private String message;
}
