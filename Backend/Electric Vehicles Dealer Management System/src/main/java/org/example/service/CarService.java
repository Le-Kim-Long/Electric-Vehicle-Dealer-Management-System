package org.example.service;

import org.example.dto.AddCarToDealerRequest;
import org.example.dto.CarResponse;
import org.example.dto.CreateCarRequest;
import org.example.dto.CreateCompleteCarRequest;
import org.example.dto.UpdateManufacturerPriceRequest;
import java.util.List;

public interface CarService {
    List<CarResponse> getAllCarsByCurrentDealer(String email);
    List<CarResponse> searchCarsByVariantOrModelName(String email, String searchTerm);

    // Methods for Admin/EVMStaff
    List<CarResponse> getAllCarsInSystem();
    List<CarResponse> searchCarsInSystem(String searchTerm);

    // Method to add new car to system (Admin/EVMStaff only)
    CarResponse addCarToSystem(CreateCarRequest request);

    // Method to add complete car with all info (Admin/EVMStaff only)
    CarResponse addCompleteCarToSystem(CreateCompleteCarRequest request);

    // Method to add car to dealer
    String addCarToDealer(AddCarToDealerRequest request);

    // Method to update manufacturer price by model, variant, and color name
    String updateManufacturerPrice(String modelName, String variantName, String colorName, UpdateManufacturerPriceRequest request);

    // Method to get manufacturer price by model, variant, and color name
    Long getManufacturerPrice(String modelName, String variantName, String colorName);
}
