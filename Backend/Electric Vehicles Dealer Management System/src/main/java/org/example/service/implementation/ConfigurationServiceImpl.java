package org.example.service.implementation;

import org.example.dto.ConfigurationResponse;
import org.example.entity.CarVariant;
import org.example.entity.Configuration;
import org.example.repository.CarVariantRepository;
import org.example.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
                .trunkVolumeL(config.getTrunkVolumeL())
                .seats(config.getSeats())
                .build();
    }

    @Override
    public ConfigurationResponse getConfigurationByVariantName(String variantName) {
        // Tìm variant với configuration theo variant name
        CarVariant carVariant = carVariantRepository.findByVariantNameIgnoreCaseAndModelId(variantName, null)
                .orElse(null);

        // Nếu không tìm thấy bằng cách trên, thử tìm bằng query đơn giản hơn
        if (carVariant == null) {
            // Tìm tất cả variants có tên giống nhau (có thể thuộc nhiều model khác nhau)
            // Trong trường hợp này, chúng ta lấy variant đầu tiên tìm được
            List<CarVariant> variants = carVariantRepository.findAll().stream()
                    .filter(v -> v.getVariantName().equalsIgnoreCase(variantName))
                    .toList();

            if (variants.isEmpty()) {
                throw new RuntimeException("Variant not found with name: " + variantName);
            }

            carVariant = variants.get(0); // Lấy variant đầu tiên
        }

        Configuration config = carVariant.getConfiguration();

        if (config == null) {
            throw new RuntimeException("Configuration not found for variant name: " + variantName);
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
                .trunkVolumeL(config.getTrunkVolumeL())
                .seats(config.getSeats())
                .build();
    }
}
