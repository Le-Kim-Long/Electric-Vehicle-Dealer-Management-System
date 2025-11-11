package org.example.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateOrderPaymentMethodRequest {
    private String paymentMethod;
}
