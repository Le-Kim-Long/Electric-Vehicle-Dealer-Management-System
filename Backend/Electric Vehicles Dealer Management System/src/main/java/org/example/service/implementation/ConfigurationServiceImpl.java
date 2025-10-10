package org.example.service.implementation;

import org.example.dto.ConfigurationResponse;
import org.example.entity.CarVariant;
import org.example.entity.Configuration;
import org.example.repository.CarVariantRepository;
import org.example.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    @Autowired
    private CarVariantRepository carVariantRepository;

    @Override
    public ConfigurationResponse getConfigurationByVariantId(Integer variantId) {
        // Tìm variant với configuration
        CarVariant carVariant = carVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found with ID: " + variantId));

        Configuration config = carVariant.getConfiguration();

        if (config == null) {
            throw new RuntimeException("Configuration not found for variant ID: " + variantId);
        }

        return ConfigurationResponse.builder()
                .configId(config.getConfigId())
                .variantId(carVariant.getVariantId())
                .modelName(carVariant.getCarModel().getModelName())
                .variantName(carVariant.getVariantName())
                .batteryCapacity(config.getBatteryCapacity())
                .batteryType(config.getBatteryType())
                .fullChargeTime(config.getFullChargeTime())
                .rangeKm(config.getRangeKm())
                .power(config.getPower())
                .torque(config.getTorque())
                .lengthMm(config.getLengthMm())
                .widthMm(config.getWidthMm())
                .heightMm(config.getHeightMm())
                .wheelbaseMm(config.getWheelbaseMm())
                .weightKg(config.getWeightKg())
                .seats(config.getSeats())
                .build();
    }
}

