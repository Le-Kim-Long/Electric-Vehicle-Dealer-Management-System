package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealerResponse {
    private Integer dealerId;
    private String dealerName;
    private String address;
    private String phone;
    private String email;
}
