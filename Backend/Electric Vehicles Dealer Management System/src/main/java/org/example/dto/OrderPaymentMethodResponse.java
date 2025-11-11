package org.example.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPaymentMethodResponse {
    private Integer orderId;
    private String paymentMethod;
}
