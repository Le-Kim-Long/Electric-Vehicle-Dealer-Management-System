package org.example.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarVariantResponse {
    private Integer variantId;
    private String variantName;
    private String description;
    private Integer modelId;
}
