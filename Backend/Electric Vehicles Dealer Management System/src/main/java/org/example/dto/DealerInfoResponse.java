package org.example.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DealerInfoResponse {
    private Integer dealerId;
    private String dealerName;
    private String dealerAddress;
    private String dealerPhone;
}

