package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompleteCarRequest {

    @NotNull(message = "Model information is required")
    private ModelInfo model;

    @NotNull(message = "Variant information is required")
    private VariantInfo variant;

    @NotNull(message = "Configuration information is required")
    private ConfigurationInfo configuration;

    @NotBlank(message = "Color is required")
    private String color;

    @NotNull(message = "Car information is required")
    private CarInfo car;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelInfo {
        @NotBlank(message = "Model name is required")
        private String modelName;

        @NotBlank(message = "Segment is required")
        private String segment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantInfo {
        @NotBlank(message = "Variant name is required")
        private String variantName;

        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigurationInfo {
        @NotNull(message = "Battery capacity is required")
        @DecimalMin(value = "0.1", message = "Battery capacity must be greater than 0")
        private Double batteryCapacity;

        @NotBlank(message = "Battery type is required")
        private String batteryType;

        @NotNull(message = "Full charge time is required")
        @Min(value = 1, message = "Full charge time must be greater than 0")
        private Integer fullChargeTime;

        @NotNull(message = "Range is required")
        @Min(value = 1, message = "Range must be greater than 0")
        private Integer rangeKm;

        @NotNull(message = "Power is required")
        @Min(value = 1, message = "Power must be greater than 0")
        private Double power;

        @NotNull(message = "Torque is required")
        @Min(value = 1, message = "Torque must be greater than 0")
        private Double torque;

        @NotNull(message = "Length is required")
        @Min(value = 1, message = "Length must be greater than 0")
        private Integer lengthMm;

        @NotNull(message = "Width is required")
        @Min(value = 1, message = "Width must be greater than 0")
        private Integer widthMm;

        @NotNull(message = "Height is required")
        @Min(value = 1, message = "Height must be greater than 0")
        private Integer heightMm;

        @NotNull(message = "Wheelbase is required")
        @Min(value = 1, message = "Wheelbase must be greater than 0")
        private Integer wheelbaseMm;

        @NotNull(message = "Weight is required")
        @Min(value = 1, message = "Weight must be greater than 0")
        private Integer weightKg;

        @NotNull(message = "Trunk volume is required")
        @Min(value = 1, message = "Trunk volume must be greater than 0")
        private Integer trunkVolumeL;

        @NotNull(message = "Number of seats is required")
        @Min(value = 1, message = "Number of seats must be at least 1")
        @Max(value = 10, message = "Number of seats cannot exceed 10")
        private Integer seats;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarInfo {
        @NotNull(message = "Production year is required")
        @Min(value = 2000, message = "Production year must be at least 2000")
        @Max(value = 2100, message = "Production year must not exceed 2100")
        private Integer productionYear;

        @NotNull(message = "Price is required")
        @Min(value = 0, message = "Price must be greater than or equal to 0")
        private Long price;

        private String imagePath;
    }
}
