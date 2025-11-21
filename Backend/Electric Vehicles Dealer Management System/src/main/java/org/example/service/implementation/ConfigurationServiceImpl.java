package org.example.service.implementation;

import org.example.dto.ConfigurationResponse;
import org.example.dto.UpdateConfigurationRequest;
import org.example.entity.CarVariant;
import org.example.entity.Configuration;
import org.example.repository.CarVariantRepository;
import org.example.repository.ConfigurationRepository;
import org.example.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    @Autowired
    private CarVariantRepository carVariantRepository;

    @Autowired
    private ConfigurationRepository configurationRepository;

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
    public ConfigurationResponse getConfigurationByModelNameAndVariantName(String modelName, String variantName) {
        // Tìm variant theo cả model name và variant name
        CarVariant carVariant = carVariantRepository.findAll().stream()
                .filter(v -> v.getCarModel().getModelName().equalsIgnoreCase(modelName)
                        && v.getVariantName().equalsIgnoreCase(variantName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Variant not found with model: " + modelName + " and variant: " + variantName));

        Configuration config = carVariant.getConfiguration();

        if (config == null) {
            throw new RuntimeException("Configuration not found for model: " + modelName + " and variant: " + variantName);
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
    public ConfigurationResponse updateConfigurationByModelNameAndVariantName(String modelName, String variantName, UpdateConfigurationRequest request) {
        // Tìm variant theo cả model name và variant name
        CarVariant carVariant = carVariantRepository.findAll().stream()
                .filter(v -> v.getCarModel().getModelName().equalsIgnoreCase(modelName)
                        && v.getVariantName().equalsIgnoreCase(variantName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Variant not found with model: " + modelName + " and variant: " + variantName));

        Configuration config = carVariant.getConfiguration();

        if (config == null) {
            throw new RuntimeException("Configuration not found for model: " + modelName + " and variant: " + variantName);
        }

        // Cập nhật các trường configuration nếu có giá trị mới
        if (request.getBatteryCapacity() != null) {
            config.setBatteryCapacity(request.getBatteryCapacity());
        }
        if (request.getBatteryType() != null) {
            config.setBatteryType(request.getBatteryType());
        }
        if (request.getFullChargeTime() != null) {
            config.setFullChargeTime(request.getFullChargeTime());
        }
        if (request.getRangeKm() != null) {
            config.setRangeKm(request.getRangeKm());
        }
        if (request.getPower() != null) {
            config.setPower(request.getPower());
        }
        if (request.getTorque() != null) {
            config.setTorque(request.getTorque());
        }
        if (request.getLengthMm() != null) {
            config.setLengthMm(request.getLengthMm());
        }
        if (request.getWidthMm() != null) {
            config.setWidthMm(request.getWidthMm());
        }
        if (request.getHeightMm() != null) {
            config.setHeightMm(request.getHeightMm());
        }
        if (request.getWheelbaseMm() != null) {
            config.setWheelbaseMm(request.getWheelbaseMm());
        }
        if (request.getWeightKg() != null) {
            config.setWeightKg(request.getWeightKg());
        }
        if (request.getTrunkVolumeL() != null) {
            config.setTrunkVolumeL(request.getTrunkVolumeL());
        }
        if (request.getSeats() != null) {
            config.setSeats(request.getSeats());
        }

        // Lưu configuration đã cập nhật
        configurationRepository.save(config);

        // Trả về response với dữ liệu đã cập nhật
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
