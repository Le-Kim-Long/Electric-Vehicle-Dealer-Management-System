package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCarToDealerRequest {
    private Integer variantId;
    private String colorName;
    private String dealerName;
    private Integer quantity;
}
