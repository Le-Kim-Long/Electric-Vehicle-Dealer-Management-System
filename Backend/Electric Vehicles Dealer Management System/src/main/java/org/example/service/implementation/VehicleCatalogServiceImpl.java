package org.example.service.implementation;

import lombok.RequiredArgsConstructor;
import org.example.dto.*;
import org.example.entity.*;
import org.example.repository.*;
import org.example.service.VehicleCatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleCatalogServiceImpl implements VehicleCatalogService {

    private final CarModelRepository modelRepo;
    private final CarVariantRepository variantRepo;
    private final CarRepository carRepo;

    @Override
    public List<CarModelResponse> getAllModels() {
        return modelRepo.findAll().stream().map(this::toModelResponse).collect(Collectors.toList());
    }

    @Override
    public CarModelResponse getModelById(Integer modelId) {
        CarModel model = modelRepo.findById(modelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Model not found"));
        return toModelResponse(model);
    }

    @Override
    public List<CarModelResponse> searchModels(String keyword) {
        return modelRepo.findByModelNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::toModelResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarVariantResponse> getVariantsByModel(Integer modelId) {
        return variantRepo.findByModelId(modelId)
                .stream()
                .map(this::toVariantResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CarVariantResponse getVariantById(Integer variantId) {
        CarVariant variant = variantRepo.findById(variantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variant not found"));
        return toVariantResponse(variant);
    }

    @Override
    public List<CarResponse> getCarsByVariant(Integer variantId) {
        return carRepo.findByVariantId(variantId)
                .stream()
                .map(this::toCarResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CarResponse getCarById(Integer carId) {
        Car car = carRepo.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
        return toCarResponse(car);
    }

    private CarModelResponse toModelResponse(CarModel model) {
        return new CarModelResponse(model.getModelId(), model.getModelName(), model.getSegment());
    }

    private CarVariantResponse toVariantResponse(CarVariant v) {
        return new CarVariantResponse(v.getVariantId(), v.getVariantName(), v.getDescription(), v.getModelId());
    }

    private CarResponse toCarResponse(Car c) {
        return new CarResponse(c.getCarId(), c.getVariantId(), c.getColorId(), c.getProductionYear(), c.getPrice());
    }
}
