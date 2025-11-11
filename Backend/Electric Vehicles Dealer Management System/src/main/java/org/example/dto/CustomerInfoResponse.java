package org.example.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerInfoResponse {
    private Integer customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
}

