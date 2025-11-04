package org.example.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarVariantResponse {
    private Integer variantId;
    private String variantName;
    private String modelName;
    private String fullName; // VF3 Eco, VF5 Plus, etc.
}
