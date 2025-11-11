package org.example.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SetDeliveryDateDto {
    @NotNull(message = "Expected delivery date is required")
    private LocalDateTime expectedDeliveryDate;
}
