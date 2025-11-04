package org.example.service;

import org.example.entity.Car;
import org.example.entity.CarModel;
import org.example.entity.CarVariant;
import org.example.entity.Color;
import org.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CarDeletionService {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarVariantRepository carVariantRepository;

    @Autowired
    private CarModelRepository carModelRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private DealerCarRepository dealerCarRepository;

    /**
     * Xóa xe theo model name, variant name, color name
     * Nếu xóa hết xe của variant đó thì tự xóa variant và configuration của variant đó
     * Nếu xóa hết các variant thì tự xóa model
     */
    @Transactional
    public String deleteCars(String modelName, String variantName, String colorName) {
        // Validate input
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new RuntimeException("Model name is required");
        }

        // Tìm model
        CarModel model = carModelRepository.findByModelNameIgnoreCase(modelName.trim())
                .orElseThrow(() -> new RuntimeException("Model not found with name: " + modelName));

        if (variantName != null && !variantName.trim().isEmpty()) {
            // Tìm variant theo model ID và variant name
            Optional<CarVariant> variantOpt = carVariantRepository.findByVariantNameIgnoreCaseAndModelId(
                variantName.trim(), model.getModelId());

            if (!variantOpt.isPresent()) {
                throw new RuntimeException("Variant '" + variantName + "' not found in model '" + modelName + "'");
            }

            CarVariant variant = variantOpt.get();

            if (colorName != null && !colorName.trim().isEmpty()) {
                // Xóa xe theo model, variant, color
                return deleteCarsByModelVariantColor(model, variant, colorName.trim());
            } else {
                // Xóa variant và tất cả xe của variant đó
                return deleteVariant(model, variant);
            }
        } else {
            // Xóa toàn bộ model và tất cả variant, xe của model đó
            return deleteModel(model);
        }
    }

    /**
     * Xóa xe theo model, variant, color
     */
    private String deleteCarsByModelVariantColor(CarModel model, CarVariant variant, String colorName) {
        // Tìm color
        Color color = colorRepository.findByColorNameIgnoreCase(colorName)
                .orElseThrow(() -> new RuntimeException("Color not found with name: " + colorName));

        // Tìm tất cả xe có variant và color tương ứng
        List<Car> carsToDelete = carRepository.findByVariantId(variant.getVariantId()).stream()
                .filter(car -> car.getColorId().equals(color.getColorId()))
                .toList();

        if (carsToDelete.isEmpty()) {
            throw new RuntimeException("No cars found with model '" + model.getModelName() +
                                     "', variant '" + variant.getVariantName() +
                                     "', color '" + colorName + "'");
        }

        int deletedCarsCount = carsToDelete.size();

        // Xóa tất cả dealer_car records trước
        for (Car car : carsToDelete) {
            dealerCarRepository.deleteByCarId(car.getCarId());
        }

        // Xóa tất cả xe
        carRepository.deleteAll(carsToDelete);

        // Kiểm tra xem variant còn xe nào không
        List<Car> remainingCars = carRepository.findByVariantId(variant.getVariantId());
        if (remainingCars.isEmpty()) {
            // Xóa configuration của variant
            if (variant.getConfiguration() != null) {
                configurationRepository.deleteById(variant.getConfiguration().getConfigId());
            }

            // Xóa variant
            carVariantRepository.deleteById(variant.getVariantId());

            // Kiểm tra xem model còn variant nào không
            List<CarVariant> remainingVariants = carVariantRepository.findByModelId(model.getModelId());
            if (remainingVariants.isEmpty()) {
                // Xóa model
                carModelRepository.deleteById(model.getModelId());
                return "Deleted " + deletedCarsCount + " cars. Variant '" + variant.getVariantName() +
                       "' and model '" + model.getModelName() + "' were also deleted as they had no remaining cars/variants.";
            }

            return "Deleted " + deletedCarsCount + " cars. Variant '" + variant.getVariantName() +
                   "' was also deleted as it had no remaining cars.";
        }

        return "Deleted " + deletedCarsCount + " cars with model '" + model.getModelName() +
               "', variant '" + variant.getVariantName() + "', color '" + colorName + "'.";
    }

    /**
     * Xóa variant và tất cả xe của variant đó
     */
    private String deleteVariant(CarModel model, CarVariant variant) {
        // Lấy tất cả xe của variant
        List<Car> carsToDelete = carRepository.findByVariantId(variant.getVariantId());
        int deletedCarsCount = carsToDelete.size();

        // Xóa tất cả dealer_car records trước
        for (Car car : carsToDelete) {
            dealerCarRepository.deleteByCarId(car.getCarId());
        }

        // Xóa tất cả xe của variant
        carRepository.deleteAll(carsToDelete);

        // Xóa configuration của variant
        if (variant.getConfiguration() != null) {
            configurationRepository.deleteById(variant.getConfiguration().getConfigId());
        }

        // Xóa variant
        carVariantRepository.deleteById(variant.getVariantId());

        // Kiểm tra xem model còn variant nào không
        List<CarVariant> remainingVariants = carVariantRepository.findByModelId(model.getModelId());
        if (remainingVariants.isEmpty()) {
            // Xóa model
            carModelRepository.deleteById(model.getModelId());
            return "Deleted variant '" + variant.getVariantName() + "' with " + deletedCarsCount +
                   " cars. Model '" + model.getModelName() + "' was also deleted as it had no remaining variants.";
        }

        return "Deleted variant '" + variant.getVariantName() + "' with " + deletedCarsCount + " cars.";
    }

    /**
     * Xóa toàn bộ model và tất cả variant, xe của model đó
     */
    private String deleteModel(CarModel model) {
        // Lấy tất cả variant của model
        List<CarVariant> variants = carVariantRepository.findByModelId(model.getModelId());
        int totalDeletedCars = 0;
        int totalDeletedVariants = variants.size();

        // Xóa tất cả xe và variant của model
        for (CarVariant variant : variants) {
            List<Car> carsToDelete = carRepository.findByVariantId(variant.getVariantId());
            totalDeletedCars += carsToDelete.size();

            // Xóa tất cả dealer_car records
            for (Car car : carsToDelete) {
                dealerCarRepository.deleteByCarId(car.getCarId());
            }

            // Xóa tất cả xe của variant
            carRepository.deleteAll(carsToDelete);

            // Xóa configuration của variant
            if (variant.getConfiguration() != null) {
                configurationRepository.deleteById(variant.getConfiguration().getConfigId());
            }

            // Xóa variant
            carVariantRepository.deleteById(variant.getVariantId());
        }

        // Xóa model
        carModelRepository.deleteById(model.getModelId());

        return "Deleted model '" + model.getModelName() + "' with " + totalDeletedVariants +
               " variants and " + totalDeletedCars + " cars.";
    }
}
