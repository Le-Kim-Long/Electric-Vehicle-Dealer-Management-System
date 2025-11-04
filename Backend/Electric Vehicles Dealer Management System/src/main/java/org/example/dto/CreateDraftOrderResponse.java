package org.example.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateDraftOrderResponse {
    private Integer orderId;
    private Integer customerId;
    private String customerName;
    private Integer dealerId;
    private String dealerName;
    private LocalDateTime orderDate;
    private String status;
    private String message;
}
