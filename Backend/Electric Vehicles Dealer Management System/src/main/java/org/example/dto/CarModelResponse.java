package org.example.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarModelResponse {
    private Integer modelId;
    private String modelName;
    private String segment;
}
