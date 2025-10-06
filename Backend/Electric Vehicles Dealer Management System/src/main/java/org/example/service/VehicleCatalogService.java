package org.example.service;

import org.example.dto.CarModelResponse;
import org.example.dto.CarVariantResponse;
import org.example.dto.CarResponse;

import java.util.List;

public interface VehicleCatalogService {
    List<CarModelResponse> getAllModels();
    CarModelResponse getModelById(Integer modelId);
    List<CarModelResponse> searchModels(String keyword);

    List<CarVariantResponse> getVariantsByModel(Integer modelId);
    CarVariantResponse getVariantById(Integer variantId);

    List<CarResponse> getCarsByVariant(Integer variantId);
    CarResponse getCarById(Integer carId);
}
